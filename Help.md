When using wget with reft its helpful to use the **--content-disposition** flag which tells wget to respect the 'Content-disposition' header. Reft uses this header to tell client the real name of a file. Without it you will download files like 'file?id=0'.

You can put that flag in your .wgetrc like so
```
$ vi ~/.wgetrc
content_disposition = on
```