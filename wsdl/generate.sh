#!/bin/bash
wsimport ecat.wsdl -b bindings.xml  -verbose  -clientjar ecat.jar -d ./target
