#!/bin/bash
set -e
#mvn -Pdoc-html
git clone -b gh-pages https://github.com/huksley/spring-boot2-template.git gh-pages
cp -rv target/generated-docs/* gh-pages/
cd gh-pages
git add --ignore-errors *
git commit -m "Updated documentation"
git push origin gh-pages
cd ..
rm -rf gh-pages target

