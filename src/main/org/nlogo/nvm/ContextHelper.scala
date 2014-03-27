
package org.nlogo.nvm

import org.nlogo.api.Let

object ContextHelper {
  def getLet(j:Job, bindigs:List[LetBinding], l:Let):java.lang.Object = {
    bindigs.find(l==_.let).map(_.value).getOrElse(j.parentContext.getLet(l))
  }
  def setLet(j:Job, bindigs:List[LetBinding], l:Let, v:java.lang.Object) {
   bindigs.find(l==_.let).map(_.value = v).getOrElse(j.parentContext.setLet(l,v))
  }
}
