abc =     "abcdefghijklmnopqrstuvwxyz"
src = "isrveawhobpnutfg"
need = "giants"

def phase_5(line):
	
	r = ""
	for c in line:
		index = ord(c) & 0xf
		r += src[index]
	return r

mmap = {
	phase_5(c): c for c in abc
}

print(mmap)
result = ""
for c in need:
    result += mmap[c]

print(result)


