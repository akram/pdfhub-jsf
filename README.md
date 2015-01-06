[![Build Status](https://travis-ci.org/mdanter/pdfhub-jsf.svg?branch=master)](https://travis-ci.org/mdanter/pdfhub-jsf)

Convert any Github wiki to a PDF! This app is geared towards content creators who need to deliver a Github wiki in cross-platform printer friendly single document format. It is the first straightforward solution to address this problem. The app resolves image URLs, preserves links, and properly formats ```<pre>``` tags for print. 

Implemented using JSF 2/Facelets in the JBoss EAP cartridge on OpenShift. The app has a light weight front end, uses no persistence layer, a @ViewScoped CDI bean, and libraries that facilitate PDF creation.
