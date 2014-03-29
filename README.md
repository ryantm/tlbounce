#tlbounce [![Build Status](https://travis-ci.org/talklibre/tlbounce.png?branch=master)](https://travis-ci.org/talklibre/tlbounce)

The [talklibre](https://github.com/talklibre/vision) IRC [bouncer](http://en.wikipedia.org/wiki/BNC_(software)).

#Overview

tlbounce will be a process running on a server that acts like the IRC client of a person. It will constantly be connected to IRC servers. This ensures the person does not miss messages that they would like to read.

tlbounce will have an HTTP API, which can be used by a client to join and part from IRC servers and channels. The API will also have an endpoint for sending messages on channels.

tlbounce will log all of the IRC PRIVMSGs it encounters in the [talklibre message format](https://github.com/talklibre/format). Messages will be saved to the person's Message Store.


