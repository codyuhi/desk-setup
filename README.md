# Desk Setup

## Summary

This repository holds the contents for a frontend-only website. The website, Desk Setup, showcases a mock interior design company that specializes in desk setups.

## Directory Contents

- `html-app`: A version of the Desk Setup application built using only HTML, CSS, and JavaScript
- `react-app`: A version of the Desk Setup application built using ReactJS

## How to run the app

There are two versions of the app, a HTML/CSS/JS version and a ReactJS version. See instructions below for how to run the app for either version:

### HTML/CSS/JS

Prerequisites:

- `http-server` NPM package installed

1. Change directory into `html-app`

```
cd html-app
```

2. Execute the `http-server` command

```
http-server
```

3. Open a browser to http://localhost:8080 and verify the page loads properly

### ReactJS

Prerequisites:

- ReactJS installed
- Yarn installed

1. Change directory into `react-app`

```
cd react-app
```

2. Install necessary Yarn dependencies

```
yarn install
```

3. Execute the node start script

```
yarn start
```

4. Open a browser to http://localhost:3000 and verify the page loads properly

### Docker

1. Build the Docker image

```
docker build -t desk-setup .
```

2. Run the Docker container and expose a port for you to access from localhost

```
docker run -p 8084:80 desk-setp
```

3. Open a browser window and navigate to localhost and the port you opened
   "http://localhost:8084"
