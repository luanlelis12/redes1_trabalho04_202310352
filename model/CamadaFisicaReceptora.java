/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 28 09 2025
* Nome.............: CamadaFisicaReceptora.java
* Funcao...........: Simular a camada fisica receptora
*************************************************************** */

package model;

public class CamadaFisicaReceptora {

  private CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora;
  private int tipoDeDecodificacao = 0;

  public CamadaFisicaReceptora (CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora) {
    this.camadaEnlaceDadosReceptora = camadaEnlaceDadosReceptora;
  }

  /* ***************************************************************
  * Metodo: getTipoDeDecodificacao
  * Funcao: retorna o tipo de decodificacao
  * Parametros: 
  * Retorno: tipo de decodificacao
  *************************************************************** */
  public int getTipoDeDecodificacao() {
    return tipoDeDecodificacao;
  } // fim do metodo getTipoDeDecodificacao
  
  /* ***************************************************************
  * Metodo: setTipoDeDecodificacao
  * Funcao: define o tipo de codificacao que sera usada
  * Parametros: tipoDeCodificacao = define um numero inteiro que representa uma codificacao
  * Retorno: void
  *************************************************************** */
  public void setTipoDeDecodificacao (int tipoDeDecodificacao) {
    this.tipoDeDecodificacao = tipoDeDecodificacao;
  } // fim do metodo setTipoDeDecodificacao

  /* ***************************************************************
  * Metodo: camadaFisicaReceptora
  * Funcao: Transformar os "sinais" em bits de acordo com o protocolo
  * Parametros: quadro = conjuntos de "sinais" a serem decodificados
  * Retorno: void
  *************************************************************** */
  public void camadaFisicaReceptora (int quadro[]) {
    int fluxoBrutoDeBits [] = null;
    switch (tipoDeDecodificacao) {
      case 0 : //codificao binaria
        fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoBinaria(quadro);
        break;
      case 1 : //codificacao manchester
        fluxoBrutoDeBits = camadaFisicaReceptoraCodificacaoManchester(quadro);
        break;
      case 2 : //codificacao manchester diferencial
        fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadro);
        break;
      }//fim do switch/case
      camadaEnlaceDadosReceptora.camadaEnlaceDadosReceptora(fluxoBrutoDeBits);
  }// fim do metodo CamadaFisicaTransmissora

  /* ***************************************************************
  * Metodo: camadaFisicaReceptoraDecodificacaoBinaria
  * Funcao: decodifica os bits do protocolo binario
  * Parametros: quadro = conjunto de bits
  * Retorno: Quadro original
  *************************************************************** */
  public int[] camadaFisicaReceptoraDecodificacaoBinaria (int quadro []) {
    return quadro;
  }// fim do metodo camadaFisicaReceptoraDecodificacaoBinaria

  /* ***************************************************************
  * Metodo: camadaFisicaReceptoraCodificacaoManchester
  * Funcao: decodifica os bits do protocolo manchester
  * Parametros: quadro = conjunto de bits
  * Retorno: Quadro original
  *************************************************************** */
  int[] camadaFisicaReceptoraCodificacaoManchester (int quadro []) {
    
    // O array de saida tera metade do tamanho do array de entrada
    int[] quadroDecodificado = new int[quadro.length/2];

    // "Ponteiro" de escrita
    int contadorBitsSaida = 0;

    // Loop para itera o quadro
    for (int pacote : quadro) {
      for (int i = 31; i >= 0; i-=2) {
        // Pega o primeiro bit do par
        int bit1 = (pacote >> i) & 1;
        // Pega o segundo bit do par
        int bit2 = (pacote >> (i-1)) & 1;

        // Calcula onde vamos escrever o bit de dado decodificado
        int indicePacoteSaida = contadorBitsSaida / 32;
        int posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);

        // Se o par de bit for 10 significa um bit 1
        if (bit1 == 1 & bit2 == 0) {
          quadroDecodificado[indicePacoteSaida] |= (1 << posicaoNoPacoteSaida);
        } // fim do if

        // Avanca o ponteiro de saida
        contadorBitsSaida++;
      } // fim do for
    } // fim do for
    return quadroDecodificado;
  }// fim do metodo CamadaFisicaReceptoraDecodificacaoManchester

  /* ***************************************************************
  * Metodo: camadaFisicaReceptoraDecodificacaoManchesterDiferencial
  * Funcao: decodifica os bits do protocolo manchester diferencial
  * Parametros: quadro = conjunto de bits
  * Retorno: Quadro original
  *************************************************************** */
  int[] camadaFisicaReceptoraDecodificacaoManchesterDiferencial(int quadro[]){
    // O array de saida tera metade do tamanho do array de entrada
    int[] quadroDecodificado = new int[quadro.length/2];

    // "Ponteiro" de escrita para o array de saida.
    int contadorBitsSaida = 0;
    // Para o primeiro bit, iremos considerar que o bit anterior eh 0
    int bitAnteriorSaida = 0; 
 
    // Loop para cada iterar o quadro
    for (int pacote : quadro) {
      for (int i = 31; i >= 0; i-=2) {
        
        // Pega o primeiro bit do par
        int bit = (pacote >> i) & 1;

        // Calcula onde vamos escrever o bit de dado decodificado
        int indicePacoteSaida = contadorBitsSaida / 32;
        int posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);

        if (bit == 1) { // O nivel no comeco do periodo e ALTO
          quadroDecodificado[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? 1 << posicaoNoPacoteSaida : 0);
          bitAnteriorSaida = (pacote >> i-1) & 1;
        } else if (bit == 0) { // O nivel no comeco do periodo e BAIXO
          quadroDecodificado[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? 0 : 1 << posicaoNoPacoteSaida);
          bitAnteriorSaida = (pacote >> i-1) & 1;
        } // fim do if
        // Avanca o ponteiro de escrita
        contadorBitsSaida++;
      } // fim do for
    } // fim do for
    return quadroDecodificado;
  }// fim do camadaFisicaReceptoraDecodificacaoManchesterDiferencial

}
