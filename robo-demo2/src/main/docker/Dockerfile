FROM java:8
VOLUME /tmp

RUN apt-get update && apt-get install -y netcat

ADD start.sh start.sh
RUN chmod +x start.sh

ADD robo-demo2-*.jar robo-demo2.jar
RUN bash -c 'touch /robo-demo2.jar'

CMD ["/start.sh"]

EXPOSE 8080