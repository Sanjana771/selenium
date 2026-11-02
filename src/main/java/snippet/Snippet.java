package snippet;

public class Snippet {
	
	# Step 1: Use Maven with Java 17
	FROM maven:3.9.6-eclipse-temurin-17 AS build
	
	# Set working directory
	WORKDIR /app
	
	# Copy pom.xml
	COPY pom.xml .
	
	# Download Maven dependencies
	RUN mvn dependency:go-offline -B
	
	# Copy source code
	COPY src ./src
	
	# Compile the tests
	RUN mvn test-compile -B
	
	
	# Step 2: Create runtime environment
	FROM eclipse-temurin:17-jre-jammy AS runtime
	
	# Set working directory
	WORKDIR /app
	
	# Install required packages
	RUN apt-get update && apt-get install -y \
	    wget \
	    gnupg \
	    curl \
	    unzip \
	    libglib2.0-0 \
	    libnss3 \
	    libfontconfig1 \
	    ca-certificates \
	    fonts-liberation \
	    && rm -rf /var/lib/apt/lists/* \
	    && curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-linux-signing-keyring.gpg \
	    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux-signing-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
	    && apt-get update && apt-get install -y google-chrome-stable \
	    && rm -rf /var/lib/apt/lists/*
	
	# Copy compiled project
	COPY --from=build /app/target /app/target
	
	# Set Chrome path
	ENV CHROME_BIN=/usr/bin/google-chrome-stable
	
	# Run Maven tests
	CMD ["mvn", "test"]
	
}

