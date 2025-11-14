/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 27 08 2025
* Nome.............: CamadaAplicacaoReceptora
* Funcao...........: Simular a camada aplicacao receptora
*************************************************************** */

package model;

import util.ConversorStringBinario;

public class CamadaAplicacaoReceptora {

  private AplicacaoReceptora aplicacaoReceptora;

  public CamadaAplicacaoReceptora(AplicacaoReceptora aplicacaoReceptora) {
    this.aplicacaoReceptora = aplicacaoReceptora;
  }

  /* ***************************************************************
  * Metodo: camadaAplicacaoReceptora
  * Funcao: decodifica os bits para a mensagem original e encaminha para camada de aplicacao receptora
  * Parametros: quadro = mensagem codificada em bits
  * Retorno: void
  *************************************************************** */
  public void camadaAplicacaoReceptora (int quadro []) {
    if (quadro == null) {
      aplicacaoReceptora.aplicacaoReceptora("");
    } else {
      // Decodifica a mensagem recebida
      String mensagem = ConversorStringBinario.decodificadorDeMensagem(quadro);
      // Envia para a proxima camada
      aplicacaoReceptora.aplicacaoReceptora(mensagem);
    } // fim do if
  }//fim do metodo camadaAplicacaoReceptora

}
