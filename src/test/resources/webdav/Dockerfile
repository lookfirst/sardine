FROM bytemark/webdav:2.4

COPY dav.conf conf/conf-available/dav.conf
COPY httpd.conf conf/httpd.conf
RUN cd /var/lib/dav/data && mkdir ./public \
 && mkdir ./testFolder && mkdir ./folderWithBasicAuth && mkdir ./lockNotImplemented
COPY test.txt /var/lib/dav/data/testFolder/
COPY test.txt /var/lib/dav/data/folderWithBasicAuth/
COPY test.txt /var/lib/dav/data/lockNotImplemented/

RUN touch "/basicAuth.pwd" && htpasswd -B -b -c "/basicAuth.pwd" jenkins jenkins
