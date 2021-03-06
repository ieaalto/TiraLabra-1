
package lib.decompressors;

import java.io.IOException;
import lib.datastructures.Dictionary;
import lib.io.IO;
import lib.io.InputBuffer;
import lib.utils.ArrayUtils;
import lib.utils.ByteAsBits;

/**
 *Purkaa LZW:llä pakatun tiedoston.
 * @author Iiro
 */
public class DecompressorLZW {
    private final IO io;
    private final Dictionary dictionary;
    private final InputBuffer inputBuffer;
    private final int dictionarySize;
    private final int entryBits;
    
    public DecompressorLZW(String input, String output, int entryBits) throws IOException{
        this.entryBits = entryBits;
        dictionarySize = (int)Math.pow(2, entryBits);
        io = new IO(input, output);
        dictionary = new Dictionary(dictionarySize);
        inputBuffer = new InputBuffer(io, entryBits*3+1);
    }
    
    public void decompress() throws IOException{     
        inputBuffer.read();
        byte[] entry = readNext();
        byte[] w = entry;
        while(true){
            try{
                entry = readNext();
            } catch(RuntimeException e){
                entry = ArrayUtils.combine(w,new byte[]{w[0]});
                for(byte b : entry){io.write(new ByteAsBits(b));}
                dictionary.add(entry);
                w = entry;
                inputBuffer.read();
                continue;
            }            
            if(entry == null){break;}
            dictionary.add(ArrayUtils.combine(w, new byte[]{entry[0]}));
            w = entry;          
            inputBuffer.read();
        }
       io.close();
    }
    /**
     * Lukee seuraavan hakemistomerkinnän.
     * @return indeksiä vastaava tavujono.
     * @throws IOException 
     */
    private byte[] readNext() throws IOException {
        boolean[] bits = inputBuffer.nextBits(entryBits);
        if(bits == null) { return null;}
        int k = ArrayUtils.booleanArrayToInt(bits);
        
        byte[] entry = dictionary.get(k);
        if(entry == null) {throw new RuntimeException();}
        for(byte b : entry){
            io.write(new ByteAsBits(b));
        }
        return entry;
    }
    
    
}
