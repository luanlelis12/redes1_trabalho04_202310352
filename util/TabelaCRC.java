/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 29 10 2025
* Ultima alteracao.: 29 10 2025
* Nome.............: TabelaCRC
* Funcao...........: Gera a tabela de consulta para o crc
*************************************************************** */

package util;

public class TabelaCRC {
  
  // Polinomio padrao (IEEE 802.3) para CRC-32
  static final int CRC32_POLINOMIO = 0xEDB88320;
  // Tabela de consulta para o calculo mais rapido
  private static final int[] CRC32_TABELA = new int[256];
 
  // Gerador da tabela
  static {
    // Iteracao para cada valor de byte possivel (0 a 255)
    for (int i=0; i<256; i++) {
      int crc = i;
      for (int j=0; j<8; j++) {
        // Verifica se o bit menos significativo (o da direita) e 1
        if ((crc & 1) == 1) {
          // Se for 1, faz o shift e o XOR com o polinomio
          crc = (crc >>> 1) ^ CRC32_POLINOMIO;
        } else {
          // Se for 0, e so um shift
          crc = (crc >>> 1);
        } // fim do if
      } // fim do for
      // Armazena o resultado final na tabela
      getCrc32Tabela()[i] = crc;
    } // fim do for
  } // fim da geracao da tabela

  /* ***************************************************************
  * Metodo: getCrc32Tabela
  * Funcao: Retorna a tabela pre calculada de crc32
  * Parametros: 
  * Retorno: Tabela com os pre calculos
  *************************************************************** */
  public static int[] getCrc32Tabela() {
    return CRC32_TABELA;
  } // fim do metodo getCrc32Tabela
}
