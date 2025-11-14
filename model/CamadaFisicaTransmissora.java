/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 28 09 2025
* Nome.............: CamadaFisicaTransmissora.java
* Funcao...........: Simular a camada fisica transmissora
*************************************************************** */

package model;

public class CamadaFisicaTransmissora {

  private MeioDeComunicacao meioDeComunicacao;
  private int tipoDeCodificacao = 0;
  private String meuId;

  public CamadaFisicaTransmissora (MeioDeComunicacao meioDeComunicacao, String id) {
    this.meioDeComunicacao = meioDeComunicacao;
    this.meuId = id;
  }

  /* ***************************************************************
  * Metodo: setTipoDeCodificacao
  * Funcao: define o tipo de codificacao
  * Parametros: tipoDeCodificacao = qual protocolo serah usado
  * Retorno: void
  *************************************************************** */
  public void setTipoDeCodificacao (int tipoDeCodificacao) {
    this.tipoDeCodificacao = tipoDeCodificacao;
  } // fim do metodo setTipoDeCodificacao

  /* ***************************************************************
  * Metodo: camadaFisicaTransmissora
  * Funcao: codificar os bits em "sinais" de acordo com o protocolo
  * Parametros: quadro = conjunto de bits
  * Retorno: void
  *************************************************************** */
  public void camadaFisicaTransmissora (int quadro[]) {
    int fluxoBrutoDeBits [] = null;
    switch (tipoDeCodificacao) {
      case 0 : //codificao binaria
        fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoBinaria(quadro);
        break;
      case 1 : //codificacao manchester
        fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoManchester(quadro);
        break;
      case 2 : //codificacao manchester diferencial
        fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro);
        break;
      }// fim do switch/case
      meioDeComunicacao.meioDeComunicacao(fluxoBrutoDeBits, this.meuId);
  }// fim do metodo camadaFisicaTransmissora

  /* ***************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoBinaria
  * Funcao: Codificar os bits de acordo com o protocolo binario
  * Parametros: quadro = conjunto de bits
  * Retorno: O mesmo quadro recebido
  *************************************************************** */
  int[] camadaFisicaTransmissoraCodificacaoBinaria (int quadro []) {
    return quadro;
  }// fim do metodoCamadaFisicaTransmissoraCodificacaoBinaria

  /* ***************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchester
  * Funcao: Codificar os bits de acordo com o protocolo manchester
  * Parametros: quadro = conjunto de bits
  * Retorno: Quadro codificado pelo protocolo Manchester
  *************************************************************** */
  int[] camadaFisicaTransmissoraCodificacaoManchester (int quadro []) {

    // Prepara o array de saida. Manchester dobra o numero de bits
    int[] pacoteManchester = new int[quadro.length * 2];

    // "Ponteiro" mestre para a escrita no array de saida
    int contadorBitsSaida = 0; 

    // Loop por cada 'int' (pacote) no array de entrada 'quadro'
    for (int pacote : quadro) {
      for (int i = 31; i >= 0; i--) {

        // Le o bit de entrada
        int bit = (pacote >> i) & 1;
                
        // Calcula onde o bit de saida deve ser escrito
        int indicePacoteSaida = contadorBitsSaida / 32;
        int posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);

        // bit de entrada e '1', saida deve ser '10'
        if (bit == 1) {
                   
          // Escreve o '1' (da transicao '10')
          pacoteManchester[indicePacoteSaida] |= (1 << posicaoNoPacoteSaida);
          // Avanca o ponteiro de saida
          contadorBitsSaida++;

          indicePacoteSaida = contadorBitsSaida / 32;
          posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);

          // Como o zero nao precisa ser escrito ele so avanca o ponteiro de saida
          contadorBitsSaida++;

        } else { // bit de entrada e '0', saida deve ser '01'
                    
          // Como o zero nao precisa ser escrito ele so avanca o ponteiro de saida
          contadorBitsSaida++;
                    
          indicePacoteSaida = contadorBitsSaida / 32;
          posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);
          
          // Escreve o '1' (da transicao '10')
          pacoteManchester[indicePacoteSaida] |= (1 << posicaoNoPacoteSaida);
          // Avanca o ponteiro de saida
          contadorBitsSaida++;
        } // fim do if
      } // fim do for
    } // fim do for
    return pacoteManchester;
  }// fim do metodo CamadaFisicaTransmissoraCodificacaoManchester

  /* ***************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchesterDiferencial
  * Funcao: codificar os bits de acordo com o protocolo manchester diferencial
  * Parametros: quadro = conjunto de bits
  * Retorno: int[]
  *************************************************************** */
  int[] camadaFisicaTransmissoraCodificacaoManchesterDiferencial(int quadro []){

    // Prepara o array de saida. Manchester Diferencial dobra o numero de bits
    int[] pacoteManchesterDiferencial = new int[quadro.length*2];
    
    // Calcula onde o bit de saida deve ser escrito
    int contadorBitsSaida = 0;
    // Guarda o ultimo NIVEL de sinal que foi enviado (0=Baixo, 1=Alto).
    int bitAnteriorSaida = 0;

    // Loop para cada 'int' no array de entrada
    for (int pacote : quadro) {
      for (int i = 31; i >= 0; i--) {

        // Le o bit de entrada
        int bit = (pacote >> i) & 1;
                
        // Calcula onde o bit de saida deve ser escrito
        int indicePacoteSaida = contadorBitsSaida / 32;
        int posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);
        
        // Se o bit de dados e '1', NAO ha transicao no inicio.
        if (bit == 1) {
          // Escreve o primeiro bit (IGUAL ao anterior)
          pacoteManchesterDiferencial[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? (1<<posicaoNoPacoteSaida) : 0); 
          // Avanca o ponteiro de escrita
          contadorBitsSaida++;

          // Recalcula o ponteiro para o SEGUNDO bit (transicao do meio)
          indicePacoteSaida = contadorBitsSaida / 32;
          posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);

          // Escreve o segundo bit (OPOSTO ao anterior)
          pacoteManchesterDiferencial[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? 0 : (1<<posicaoNoPacoteSaida)); 
          
          // Atualiza a memoria. O novo "ultimo nivel" e o oposto.
          bitAnteriorSaida = ((bitAnteriorSaida == 1) ? 0 : 1);
          contadorBitsSaida++;
        } else if (bit == 0) { // Se o bit de dados e '0', HA transicao no inicio.
          // Escreve o primeiro bit (OPOSTO ao anterior)
          pacoteManchesterDiferencial[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? 0 : (1<<posicaoNoPacoteSaida)); 
          // Avanca o ponteiro
          contadorBitsSaida++;

          // Recalcula o ponteiro para o SEGUNDO bit (transicao do meio)
          indicePacoteSaida = contadorBitsSaida / 32;
          posicaoNoPacoteSaida = 31 - (contadorBitsSaida % 32);
          
          // Escreve o segundo bit (IGUAL ao anterior)
          pacoteManchesterDiferencial[indicePacoteSaida] |= ((bitAnteriorSaida == 1) ? (1<<posicaoNoPacoteSaida) : 0); 
          
          // Atualiza a memoria. O novo "ultimo nivel" e o oposto.
          bitAnteriorSaida = ((bitAnteriorSaida == 1) ? 1 : 0);
          contadorBitsSaida++;
        } // fim do if
      } // fim do for
    } // fim do for
    return pacoteManchesterDiferencial;
  }// fim do CamadaFisicaTransmissoraCodificacaoManchesterDiferencial

}