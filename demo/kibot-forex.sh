#!/bin/bash


getdata () {
   echo -e "Getting Data.."
   curl -XGET -L --verbose \
      "https://www.kibot.com/download.aspx?product=5,All_Forex_Pairs_1min" 
}

getdata
