#!/bin/zsh

docker build --tag 'desk-setup' . 
docker run -p 80:80 'desk-setup'