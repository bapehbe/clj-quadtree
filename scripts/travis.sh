#!/bin/sh
lein2 midje
if [ $? -eq 0 ];
    then lein2 perforate;
fi
