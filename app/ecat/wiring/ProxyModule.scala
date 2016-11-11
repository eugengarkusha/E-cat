package ecat.wiring

import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import async.client.ObmenSaitPortType

trait ProxyModule {
  lazy val proxy: ObmenSaitPortType = {
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait")
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555")
    srv
  }

}
