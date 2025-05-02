APP_NAME := app
GO_FILES := main.go
IMAGE := docker.io/daveankin/udp-echo

.PHONY: all clean

all: $(APP_NAME)

$(APP_NAME): $(GO_FILES)
	GOOS=linux CGO_ENABLED=0 go build -ldflags="-s -w -extldflags '-static'" -o $(APP_NAME) $(GO_FILES)

clean:
	rm -f $(APP_NAME)

.PHONY: container
container: $(APP_NAME)
	docker build -t $(IMAGE) --progress plain .

.PHONY: container_upload
container_upload: container
	docker push $(IMAGE)
