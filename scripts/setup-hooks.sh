#!/bin/bash

git config core.hooksPath .githooks

chmod +x .githooks/commit-msg

echo "Git hooks installed"