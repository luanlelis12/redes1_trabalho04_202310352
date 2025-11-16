/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 28 10 2025
* Nome.............: CamadaAplicacaoTransmissora
* Funcao...........: Simular a camada aplicacao transmissora
*************************************************************** */

package model;

import java.util.List;

import util.Auxiliar;
import util.ConversorStringBinario;

public class CamadaAplicacaoTransmissora {
  
  private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora;
  
  public CamadaAplicacaoTransmissora(CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora) {
    this.camadaEnlaceDadosTransmissora = camadaEnlaceDadosTransmissora;
  }

  /* ***************************************************************
  * Metodo: camadaDeAplicacaoTransmissora
  * Funcao: codificar a mensagem enviada e enviar o pacote de binarios para a camada fisica
  * Parametros: mensagem = mensagem enviada pelo transmissor
  * Retorno: void
  *************************************************************** */
  public void camadaDeAplicacaoTransmissora (String mensagem) {
    Auxiliar auxiliar = new Auxiliar();
    // Define o tamanho dos pacotes em 4 caracteres
    int tamanhoDoPacote = 3;

    // Cria uma lista com a mensagem dividida em pacotes de 4 caracteres
    List<String> pacotesGerados = auxiliar.segmentarMensagem(mensagem, tamanhoDoPacote);

    // Itera todos os pacotes codificando eles e depois enviando para a camada de enlace de dados
    for (String pacote : pacotesGerados) {
      // codifica o pacote para binario
      int pacoteCodificado [] = ConversorStringBinario.codificadorDeMensagem(pacote);
      // chama a proxima camada
      camadaEnlaceDadosTransmissora.camadaEnlaceDadosTransmissora(pacoteCodificado);
    } // fim do for

  }//fim do metodo CamadaDeAplicacaoTransmissora
  
}