#!/usr/bin/env bash
# caching is hard to deal with in this approach
#npx nodemon -w 'source/**' -e '*' --exec "rm -rf build ; make dirhtml && hs build/dirhtml"

# https://pypi.org/project/sphinx-autobuild/
sphinx-autobuild source build/dirhtml
