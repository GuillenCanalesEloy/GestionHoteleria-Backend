package com.Grupo1.GestionHoteleria_Backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", ".jpg",
			"image/png", ".png",
			"image/webp", ".webp"
	);

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

	private final Path uploadDir;
	private final HttpClient httpClient;
	private final String supabaseUrl;
	private final String supabaseServiceRoleKey;
	private final String supabaseBucket;

	public FileStorageService(
			@Value("${app.upload-dir:uploads/habitaciones}") String uploadDir,
			@Value("${app.supabase.url:}") String supabaseUrl,
			@Value("${app.supabase.service-role-key:}") String supabaseServiceRoleKey,
			@Value("${app.supabase.storage.bucket:habitaciones}") String supabaseBucket
	) {
		this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
		this.httpClient = HttpClient.newHttpClient();
		this.supabaseUrl = removeTrailingSlash(supabaseUrl);
		this.supabaseServiceRoleKey = supabaseServiceRoleKey;
		this.supabaseBucket = supabaseBucket;
	}

	public String storeHabitacionImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		String extension = resolveExtension(file);
		String filename = UUID.randomUUID() + extension;
		String contentType = resolveContentType(file, extension);

		if (isSupabaseConfigured()) {
			return storeInSupabase(file, "habitaciones/" + filename, contentType);
		}

		return storeLocally(file, filename);
	}

	private String storeInSupabase(MultipartFile file, String objectPath, String contentType) {
		String encodedObjectPath = encodePath(objectPath);
		URI uploadUri = URI.create("%s/storage/v1/object/%s/%s".formatted(
				supabaseUrl,
				encodeSegment(supabaseBucket),
				encodedObjectPath
		));

		try {
			HttpRequest request = HttpRequest.newBuilder(uploadUri)
					.header("Authorization", "Bearer " + supabaseServiceRoleKey)
					.header("apikey", supabaseServiceRoleKey)
					.header("Content-Type", contentType)
					.header("x-upsert", "false")
					.POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("No se pudo subir la imagen a Supabase Storage: " + response.body());
			}

			return "%s/storage/v1/object/public/%s/%s".formatted(
					supabaseUrl,
					encodeSegment(supabaseBucket),
					encodedObjectPath
			);
		} catch (IOException exception) {
			throw new IllegalStateException("No se pudo leer la imagen para subirla a Supabase Storage", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("La subida de imagen a Supabase Storage fue interrumpida", exception);
		}
	}

	private String storeLocally(MultipartFile file, String filename) {
		Path destination = uploadDir.resolve(filename).normalize();

		if (!destination.startsWith(uploadDir)) {
			throw new IllegalArgumentException("Nombre de archivo invalido");
		}

		try {
			Files.createDirectories(uploadDir);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
			}
			return "/uploads/habitaciones/" + filename;
		} catch (IOException exception) {
			throw new IllegalStateException("No se pudo guardar la imagen", exception);
		}
	}

	private boolean isSupabaseConfigured() {
		return !supabaseUrl.isBlank() && !supabaseServiceRoleKey.isBlank();
	}

	private String resolveExtension(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType != null) {
			String extension = EXTENSIONS_BY_CONTENT_TYPE.get(contentType.toLowerCase(Locale.ROOT));
			if (extension != null) {
				return extension;
			}
		}

		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
		String extension = "";
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex >= 0) {
			extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
		}

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WEBP");
		}
		return ".jpeg".equals(extension) ? ".jpg" : extension;
	}

	private String resolveContentType(MultipartFile file, String extension) {
		String contentType = file.getContentType();
		if (contentType != null && EXTENSIONS_BY_CONTENT_TYPE.containsKey(contentType.toLowerCase(Locale.ROOT))) {
			return contentType.toLowerCase(Locale.ROOT);
		}
		return switch (extension) {
			case ".png" -> "image/png";
			case ".webp" -> "image/webp";
			default -> "image/jpeg";
		};
	}

	private String encodePath(String path) {
		return path.lines()
				.findFirst()
				.orElse("")
				.replace("\\", "/")
				.transform(value -> {
					String[] segments = value.split("/");
					StringBuilder encoded = new StringBuilder();
					for (int i = 0; i < segments.length; i++) {
						if (i > 0) {
							encoded.append("/");
						}
						encoded.append(encodeSegment(segments[i]));
					}
					return encoded.toString();
				});
	}

	private String encodeSegment(String segment) {
		return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private String removeTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
