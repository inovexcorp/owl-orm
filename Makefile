.phony: build add_license_headers

MVN_ARGS := "-T 1.5C"

build:
	mvn clean install ${MVN_ARGS}

add_license_headers:
	mvn license:format ${MVN_ARGS}