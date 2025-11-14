/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 25 08 2025
* Nome.............: AplicacaoReceptora
* Funcao...........: Simular a aplicacao receptora
*************************************************************** */

package model;

public class AplicacaoReceptora {

  private String mensagem="";

  public AplicacaoReceptora() {}

  /* ***************************************************************
  * Metodo: AplicacaoReceptora
  * Funcao: recebe uma String e define como mensagem a ser recebida
  * Parametros: mensagem = String contendo a mensagem
  * Retorno: void
  *************************************************************** */
  public void aplicacaoReceptora (String mensagem) {
    this.mensagem += mensagem;
  }//fim do metodo AplicacaoReceptora

  /* ***************************************************************
  * Metodo: getMensagem
  * Funcao: retorna uma String contendo a mensagem enviada pelo transmissor
  * Parametros: 
  * Retorno: mensagem recebida
  *************************************************************** */
  public String getMensagem () {
    return mensagem;
  } // fim do metodo getMensagem

  /* ***************************************************************
  * Metodo: apagarMensagem
  * Funcao: limpa a mensagem anterior para imprimir a próxima
  * Parametros: 
  * Retorno: void
  *************************************************************** */
  public void apagarMensagem () {
    mensagem = "";
  } // fim do metodo apagarMensagem
  
}
