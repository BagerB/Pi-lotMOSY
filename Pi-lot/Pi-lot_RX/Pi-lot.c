#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<arpa/inet.h>
#include<sys/socket.h>

#include <pigpio.h>

#define BUFLEN 32			//UDP BufferSize
#define PORT 8888			//UDP Port
#define SERVOCENTER 1500	//Neutrale Position der Servos
#define SERVORANGE 500		//Maximaler Servoausschlag
#define PINGAS 16			//GPIO-Pin des Raspberry Pi's für den Motor
#define PINSTEER 20			//GPIO-Pin des Raspberry Pi's für den Lenkservo
#define PINLOOK 21			//GPIO-Pin des Raspberry Pi's für den Kameraservo

#define DEBUG 0

//Cast von 4 ByteArray zu Float
union rcdata{
	unsigned char c[3];
	float f;
};

int main(void)
{
    struct sockaddr_in si_me, si_other;
    struct timeval read_timeout;

    union rcdata rcd_steer;
    union rcdata rcd_look;
    union rcdata rcd_gas;
    union rcdata rcd_brake;

    read_timeout.tv_sec = 0;
    read_timeout.tv_usec = 100000;

    int s, slen = sizeof(si_other) , recv_len;
    unsigned short gas, steer, look;
    char buf[BUFLEN];

    printf("Pi-Lot RX\n\n");

	//Initialisierung der pigpio library
    if (gpioInitialise() < 0)
    {
    	printf("pigpio error\n");
    	exit(1);
    } else {
    	printf("pigpio initialised\n");
    }

	//Initialisierung des UDP-Socket
    if ((s=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
    	printf("Socket error\n");
    	exit(1);
    } else {
    	printf("Socket created\n");
    }

    //Setzt die Optionen für den UDP-Socket (Timeout aktiviert)
    setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, &read_timeout, sizeof read_timeout);

    
    memset((char *) &si_me, 0, sizeof(si_me));

    si_me.sin_family = AF_INET;
    si_me.sin_port = htons(PORT);
    si_me.sin_addr.s_addr = htonl(INADDR_ANY);

    if( bind(s , (struct sockaddr*)&si_me, sizeof(si_me) ) == -1)
    {
    	printf("Bind error \n");
    	exit(1);
    }

    printf("running ...\n");

    while(1)
    {
        fflush(stdout);

        //Bei Timeout werden alle Werte auf Null gesetzt (Motor aus, neutrale Position der Servos)
        if ((recv_len = recvfrom(s, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == -1)
        {
        	rcd_steer.f = 0.0;
        	rcd_look.f = 0.0;
        	rcd_gas.f = 0.0;
        	rcd_brake.f = 0.0;
        } else {
        	rcd_steer.c[0] = buf[0];
        	rcd_steer.c[1] = buf[1];
        	rcd_steer.c[2] = buf[2];
        	rcd_steer.c[3] = buf[3];

        	rcd_look.c[0] = buf[4];
        	rcd_look.c[1] = buf[5];
        	rcd_look.c[2] = buf[6];
        	rcd_look.c[3] = buf[7];

        	rcd_gas.c[0] = buf[8];		
        	rcd_gas.c[1] = buf[9];
        	rcd_gas.c[2] = buf[10];
        	rcd_gas.c[3] = buf[11];

        	rcd_brake.c[0] = buf[12];
        	rcd_brake.c[1] = buf[13];
        	rcd_brake.c[2] = buf[14];
        	rcd_brake.c[3] = buf[15];
        }

		//Setzt die Werte in Servopulssignale um
        gas = SERVOCENTER + ((SERVORANGE * rcd_gas.f)-(SERVORANGE * rcd_brake.f));
        steer = SERVOCENTER + SERVORANGE * rcd_steer.f;
        look = SERVOCENTER + SERVORANGE * rcd_look.f;

		
        gpioServo(PINGAS, gas);
        gpioServo(PINSTEER, steer);
        gpioServo(PINLOOK, look);

        if(DEBUG){
        	printf("Received packet from %s:%d\n", inet_ntoa(si_other.sin_addr), ntohs(si_other.sin_port));
        	//printf("Data: %s\n" , buf);
        	printf("Data: %f %f %f %f\n",rcd_steer.f,rcd_look.f,rcd_gas.f,rcd_brake.f);
        	printf("Servo: %i %i %i \n",gas,steer,look);
        }
    }
    gpioTerminate();
    return 0;
}
