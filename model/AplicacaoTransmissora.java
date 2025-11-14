/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 25 08 2025
* Nome.............: AplicacaoTransmissora
* Funcao...........: Simular a aplicacao transmissora
*************************************************************** */

package model;

public class AplicacaoTransmissora {

  private CamadaAplicacaoTransmissora camadaAplicacaoTransmissora;

  public AplicacaoTransmissora(CamadaAplicacaoTransmissora camadaAplicacaoTransmissora) {
    this.camadaAplicacaoTransmissora = camadaAplicacaoTransmissora;
  }

  /* ***************************************************************
  * Metodo: aplicacaoTransmissora
  * Funcao: Enviar a mensagem para a camada de aplicacao 
  * Parametros: mensagem = String contendo mensagem do transmissor
  * Retorno: void
  *************************************************************** */
  public void aplicacaoTransmissora (String mensagem) {
    // chama a proxima camada
    camadaAplicacaoTransmissora.camadaDeAplicacaoTransmissora(mensagem);
  } //fim do metodo AplicacaoTransmissora

}