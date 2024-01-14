#!/usr/bin/env bash
npx nodemon -w 'source/**' --exec "make dirhtml && hs build/dirhtml"
