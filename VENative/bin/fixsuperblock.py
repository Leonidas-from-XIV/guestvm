#!/usr/bin/python
import sys
import struct
if len(sys.argv) != 2:
	print "Usage: %s <diskfile>"
	exit(128)
f=open(sys.argv[1],"r+b")
f.seek(1082)
f.write(struct.pack("B",1))
f.close()
