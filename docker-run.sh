#!/bin/sh
docker run \
       --rm \
       -it \
       --name nescala \
       -p 8080:8080 \
       -v $HOME/.sbt:/sbt \
       -v $HOME/.ivy2:/ivy2 \
       -v $PWD:/project \
       jehrhardt/sbt
