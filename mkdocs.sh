#!/bin/bash
set -e
mvn -Pdoc-html
if [ ! -d gh-pages ]; then
    if [ "$CI" == "true" ]; then
        # Use ssh URI in CI environment
        git clone -b gh-pages git@github.com:huksley/spring-boot2-template.git gh-pages
    else
        git clone -b gh-pages https://github.com/huksley/spring-boot2-template.git gh-pages
    fi
fi
cp -rv target/generated-docs/* gh-pages/
cd gh-pages

git config --global user.email "cicd@ruslan.org"
git config --global user.name "CICD"
git add --ignore-errors *
git commit -m "Updated documentation [skip ci]"
git push origin gh-pages
cd ..
rm -rf gh-pages target

