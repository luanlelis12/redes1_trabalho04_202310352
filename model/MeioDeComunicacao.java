/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 02 11 2025
* Nome.............: MeioDeComunicacao.java
* Funcao...........: Simular o meio de comunicacao
*************************************************************** */

package model;

import java.util.Random;

import util.ConversorStringBinario;
import util.MonitorDeFluxo;

public class MeioDeComunicacao {
  
  private CamadaFisicaReceptora receptorHostA;
  private CamadaFisicaReceptora receptorHostB;

  private int[] fluxoBrutoDeBits; 
  private float chanceErro = 0;
  
  private MonitorDeFluxo monitor;

  public MeioDeComunicacao(CamadaFisicaReceptora receptorHostA, CamadaFisicaReceptora receptorHostB) {
    this.receptorHostA = receptorHostA;
    this.receptorHostB = receptorHostB;
    this.monitor = new MonitorDeFluxo();
  }
  
  /* ***************************************************************
  * Metodo: setChanceErro
  * Funcao: define a variavel chanceErro
  * Parametros: chanceErro = chance para acontecer um erro
  * Retorno: void
  *************************************************************** */
  public void setChanceErro(float chanceErro) {
    this.chanceErro = chanceErro;
  }// fim do metodo setChanceErro

  /* ***************************************************************
  * Metodo: setFluxoBrutoDeBits
  * Funcao: define a variavel FluxoBrutoDeBits
  * Parametros: fluxoBrutoDeBits = bits sendo transferidos
  * Retorno: void
  *************************************************************** */
  public void setFluxoBrutoDeBits(int[] fluxoBrutoDeBits) {
    this.fluxoBrutoDeBits = fluxoBrutoDeBits;
  } // fim do metodo setFluxoBrutoDeBits

  /* ***************************************************************
  * Metodo: getFluxoBrutoDeBits
  * Funcao: retorna a variavel FluxoBrutoDeBits
  * Parametros: 
  * Retorno: fluxo de bits transmitidos
  *************************************************************** */
  public int[] getFluxoBrutoDeBits() {
    return fluxoBrutoDeBits;
  } // fim do metodo getFluxoBrutoDeBits

  /* ***************************************************************
  * Metodo: getMonitor
  * Funcao: retorna o monitor de fluxo
  * Parametros: 
  * Retorno: monitor de fluxo
  *************************************************************** */
  public MonitorDeFluxo getMonitor() {
    return monitor;
  } // fim do metodo getMonitor

  /* ***************************************************************
  * Metodo: meioDeComunicacao
  * Funcao: Este metodo simula a transmissao da informacao no meio de
  * comunicacao, passando de um pontoA (transmissor) para um
  * ponto B (receptor)
  * Parametros: fluxoBrutoDeBits = bits transferidos entre dispositivos
  * Retorno: void
  *************************************************************** */
  public void meioDeComunicacao (int fluxoBrutoDeBits [], String origem) {
    fluxoBrutoDeBits = transferirBits(fluxoBrutoDeBits);
    monitor.adicionarQuadroAoFluxo(fluxoBrutoDeBits);

    // Logica de "roteamento"
    if (origem.equals("HOST_A")) {
      // Se veio do A, entrega no B
      receptorHostB.camadaFisicaReceptora(fluxoBrutoDeBits);
    } else {
      // Se veio do B (um ACK), entrega no A
      receptorHostA.camadaFisicaReceptora(fluxoBrutoDeBits);
    }
  }//fim do metodo MeioDeComunicacao

  /* ***************************************************************
  * Metodo: transferirBits
  * Funcao: transfere bit por bit entre dispositivos
  * Parametros: fluxoBrutoDeBits = bits transferidos entre dispositivos
  * Retorno: bits enviado do transmissor para o receptor
  *************************************************************** */
  public int[] transferirBits (int[] fluxoBrutoDeBits) {
    
    // Cria um array de saida transferencia do mesmo tamanho do array de entrada
    int[] transferencia = new int[fluxoBrutoDeBits.length]; 

    // Calcula o indice do ultimo bit
    int ultimoBit = (fluxoBrutoDeBits.length*32)-1;

    // indiceBits e o ponteiro para qual int do array estamos 
    int indiceBits = 0;
    int contadorBit;

    Random random = new Random();
    // Sorteia um numero de 0 a 100 para gerar a probabilidade de erro
    int num = random.nextInt(101); // nextInt(101) gera de 0 a 100
    int bitErro = 0; // Qual bit tera o sinal invertido
    boolean erro = false; // Flag para controlar que apenas um bit tenha erro no quadro

    // Compara o numero sorteado com a chance de erro definida
    if (num < chanceErro) {
      erro = true;
      // Sorteia qual bit, de 0 ate o ultimo, sera corrompido.
      bitErro = random.nextInt(ultimoBit);
    } // fim do if

    // Transfere um bit de cada vez, do inicio ao fim.
    for (contadorBit = 0; contadorBit <= ultimoBit; contadorBit++) {
      
      // Se o contador de bits e multiplo de 32 (e nao e o 0) avanca para o proximo indice do array 
      if (contadorBit%32==0 && contadorBit!=0) {
        indiceBits++;
      } // fim do if
      
      // Le o bit atual do array de ENTRADA
      int bit = (fluxoBrutoDeBits[indiceBits] >> 31-(contadorBit%32)) & 1;

      // Verifica se e possivel ter erro e se este e o bit sorteado
      if (contadorBit == bitErro & erro == true) {
        // Inverte o bit (1 vira 0, 0 vira 1)
        bit = (bit == 1 ? 0 : 1);
      } // fim do if

      // Se o bit (original ou corrompido) for '1', escreve ele no array de SAIDA.
      if (bit == 1) {
        transferencia[indiceBits] |= bit << 31-(contadorBit%32);
      } // fim di if

    } // fim do for
    
    return transferencia;
  }// fim do metodo transferirBits 

}