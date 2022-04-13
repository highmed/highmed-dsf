Variables:

${HTTPS\_SERVER\_NAME\_PORT} - example: localhost:8443  
${APP\_SERVER\_IP} - example: 172.28.1.3  
${SSL\_CERTIFICATE\_FILE} - to set apache config param `SSLCertificateFile`  
${SSL\_CERTIFICATE\_KEY\_FILE} - to set apache config param `SSLCertificateKeyFile`  
${SSL\_CERTIFICATE\_CHAIN\_FILE} - to set apache config param `SSLCertificateChainFile` (optional)  
${SSL\_CA\_CERTIFICATE\_FILE} - to set apache config param `SSLCACertificateFile`  
${SSL\_CA\_DN\_REQUEST\_FILE} - to set apache config param `SSLCADNRequestFile` (optional)  
${PROXY\_PASS\_TIMEOUT\_HTTP} - timeout (seconds) for reverse proxy to app server http connection, time the proxy waits for a reply, default: `60` seconds  
${PROXY\_PASS\_TIMEOUT\_WS} - timeout (seconds) for reverse proxy to app server ws connection, time the proxy waits for a reply, default: `60` seconds  
${PROXY\_PASS\_CONNECTION\_TIMEOUT\_HTTP} - connection timeout (seconds) for reverse proxy to app server http connection, time the proxy waits for a connection to be established, default: `30` seconds  
${PROXY\_PASS\_CONNECTION\_TIMEOUT\_WS} - connection timeout (seconds) for reverse proxy to app server ws connection, time the proxy waits for a connection to be established, default: `30` seconds