check-deps:
	./gradlew dependencyUpdates -Drevision=release

dev:
	./gradlew run

setup:
	gradle wrapper --gradle-version 8.3

clean:
	./gradlew clean

build:
	cd app && ./gradlew clean build

start: dev

install:
	./gradlew installDist

lint:
	./gradlew checkstyleMain checkstyleTest

report:
	cd app/ && ./gradlew jacocoTestReport

test:
	./gradlew test

image-build:
	docker build -t hexletcomponents/java-javalin-example:latest .

image-push:
	docker push hexletcomponents/java-javalin-example:latest

.PHONY: build
