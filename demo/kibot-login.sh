#!/bin/bash


getdata () {
   echo -e "Getting Data.."
   curl -XGET --verbose \
      "http://api.kibot.com/?action=login&user=h@gmail.com&password=" 
}

getdata
