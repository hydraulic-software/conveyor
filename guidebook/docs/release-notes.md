# Release notes

## New features

* Conveyor will now validate the certificate chain for your Windows certificates ahead of time, and fill out any missing intermediates by using AIA extensions where possible. This should catch and fix a common source of mistakes and pain when code signing. 
