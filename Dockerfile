# Using OpenJDK11
FROM adoptopenjdk/openjdk11:alpine
RUN adduser -h /app -D exec

ADD ./build/install/%Project_Name% /app/binaries
VOLUME /app/data

# Permission Management
RUN chown -R exec:exec /app/*
RUN chmod -R 777 /app/*
USER exec
WORKDIR /app

RUN ls -AlhX

# GO
ENTRYPOINT /app/binaries/bin/%Project_Name%
