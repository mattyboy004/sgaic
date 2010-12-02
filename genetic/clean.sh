#!/bin/sh
rm generations/*
rm log/*
sqlite3 ./comps.db "update comps set in_use = 0;"
