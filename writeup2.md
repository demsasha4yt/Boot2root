# Method 2

To get started, we need to log in as a user via ssh.
The method is described in writeup1.

We exploit system vulnerabilities.
The system kernel is 3.2.0-91-generic-pae.

Since we have access to the user laurie, we will work with him.
We are looking for vulnerabilities on the website www.exploit-db.com
Finding an exploit https://www.exploit-db.com/exploits/40839

This exploit uses the "pokemon" exploit of the "dirtycow" vulnerability
as a base and automatically generates a new passwd line.
This vulnerability allows a local user to elevate their privileges
due to race error in copy-on-write (COW) implementation
for pages of memory marked with the Dirty bit flag (changed memory).

This exploit replaces the /etc/passwd file
and copies it to /tmp/passwd.bak (so as not to lose user data).

Since we need to get root access, we slightly change the file, since there is the user firefart.

Compile the file
gcc -pthread dirty.c -o dirty -lcrypt

Run ./dirty my-new-password

And log in to root via su with our new password)
Since we have ISO, it is not necessary to restore the passwd backup.


# Метод 2

Для начала нам необходимо залогиниться под пользоваетелем через ssh.
Метод описан в writeup1.

Используем уязвимости системы.
Ядро системы у нас 3.2.0-91-generic-pae.

Так как у нас есть доступ к пользователю laurie, работать будем с него.
Ищем уязвимости на сайте www.exploit-db.com
Находим эксплойт https://www.exploit-db.com/exploits/40839

Этот эксплойт использует в качестве основы эксплойт «pokemon» уязвимости dirtycow 
и автоматически генерирует новый файл /etc/passwd. 
С помощью данной уязвимости локальный пользователь может повысить свои привилегии 
из-за ошибки гонки в реализации механизма копирования при записи (COW) 
для страниц памяти, помеченных флагом Dirty bit (изменённая память).

Данный эксплойт подменяет файл /etc/passwd 
и копирует его в /tmp/passwd.bak (чтобы не потерять данные пользователей).

Так как нам нужно получить доступ к root, немного изменяем файл, т.к. нам стоит пользователь firefart.

Компилируем файл
gcc -pthread dirty.c -o dirty -lcrypt

Запускаем ./dirty my-new-password

И логинимся в root через su с нашим новым паролем)
Так как у нас ISO, восстанавливать бекап passwd необязательно.


