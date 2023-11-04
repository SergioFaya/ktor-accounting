

# Readme

## Creating a server
1. https://ktor.io/docs/engines.html#choose-create-server

## Deploying 

[Ktor gradle plugin](https://github.com/ktorio/ktor-build-plugins)

### Packaging 

* https://ktor.io/docs/deploy.html

### Containerization

* https://ktor.io/docs/docker.html

1. `gradle buildImage`
2. `docker load < build/jib-image.tar`
3. `docker run -p 8080:80 nomad.digital`