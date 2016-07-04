#!/bin/bash
wsimport ecat.wsdl -b bindings.xml  -verbose  -clientjar ecatSOAP.jar -d ./target
