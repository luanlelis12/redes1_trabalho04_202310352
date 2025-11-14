/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 28 10 2025
* Nome.............: Auxiliar
* Funcao...........: Implementar algumas funcoes para auxiliar no codigo
*************************************************************** */

package util;

import java.util.ArrayList;
import java.util.List;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;

public class Auxiliar {
  
  /****************************************************************
  * Metodo: currentStage
  * Funcao: Retornar o stage atual conforme a variavel "event"
  * Parametros: event = de onde saiu a acao
  * Retorno: O stage atual
  ****************************************************************/
  public static Stage currentStage(Event event) {
    // Retorna o Stage atual de onde ocorreu o evento
    return (Stage) ((Node) event.getSource()).getScene().getWindow();
  }

  /* ***************************************************************
  * Metodo: segmentarMensagem
  * Funcao: Divide a mensagem total em pacotes separados de acordo com o tamanho passado
  * Parametros: mensagem = texto contendoa mensagem que sera transmitida
  *             tamanhoPacote = tamanho dos pacotes que a mensagem sera dividida
  * Retorno: Lista com os pacotes da mensagem
  *************************************************************** */
  public List<String> segmentarMensagem(String mensagem, int tamanhoPacote) {
    List<String> pacotes = new ArrayList<>();

    // Itera pela mensagem, pulando de 'tamanhoPacote' em 'tamanhoPacote'
    for (int i = 0; i < mensagem.length(); i += tamanhoPacote) {

      // Calcula o indice final do pacote
      int indiceFinal = Math.min(i + tamanhoPacote, mensagem.length());

      // Extrai o pacote e o adiciona a lista
      pacotes.add(mensagem.substring(i, indiceFinal));
    } // fim do for

    return pacotes;
  } // fim do metodo segmentarMensagem

}