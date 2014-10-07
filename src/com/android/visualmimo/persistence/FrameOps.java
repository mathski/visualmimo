package com.android.visualmimo.persistence;

public class FrameOps {
	

    //For Subtraction
    public static Frame intensify(Frame frame) {
        byte[] raw = frame.getRaw();

        boolean increase = true;
        for(int i = 0; i < raw.length; i++){
            if(increase) {
                if(!(raw[i] + 10 > 255)) {
                    raw[i] += 10;
                    increase = false;
                }
            } else {
                increase = true;
            }
        }

        return new Frame(raw, frame.getWidth(), frame.getHeight());
    }

    //For division
    public static Frame scale(Frame frame) {
        byte[] raw = frame.getRaw();

        boolean increase = true;
        for(int i = 0; i < raw.length; i++){
            if(increase) {
                if(!(raw[i] + 10 > 255)) {
                    raw[i] *= 10;
                    increase = false;
                }
            } else {
                increase = true;
            }
        }

        return new Frame(raw, frame.getWidth(), frame.getHeight());
    }

    public static Frame frameSubtraction(Frame x, Frame y) {

        byte[] normal =  x.getRaw();
        byte[] intensified = y.getRaw();
        
        int len = intensified.length;
        if(normal.length < intensified.length)
            len = normal.length;

        byte[] sub = new byte[len];
			
        // Compute Subtraction
        for(int i = 0 ; i < (len); i++){
			sub[i] = (byte)( Math.abs(intensified[i] - normal[i])*10);
			// Scale resultant intensity by x10
			
			//Attempt to convert to NV21 byte array to greyscale
			// Not yet working perfectly, image is yellow
			if((i)%4 == 0){
				sub[i] = (byte)0xFF;
			}
			else{
				if((i)%4==1){
					sub[i]=(byte)Math.abs(sub[i+2]);
				}
				else if((i)%4==2){
					sub[i]=(byte)Math.abs(sub[i+1]);
				}
				else if((i)%4==3){
					sub[i]=(byte)Math.abs(sub[i]);
				}
			}
        }
        
  		 
        return new Frame(sub, x.getWidth(), x.getHeight());
    }
    
    
    
    
    
    /*/
    public static Frame frameSubtraction(Frame x, Frame y) {

        byte[] normal =  x.getRaw();
        byte[] intensified = y.getRaw();

        int len = intensified.length;
        if(normal.length < intensified.length)
            len = normal.length;
        
        byte[] sub = new byte[len];
        for(int i = 0 ; i < (len); i++){      	     	
            sub[i] = (byte)( Math.abs(intensified[i] - normal[i])*10);
        }  		 

        return new Frame(sub, x.getWidth(), x.getHeight());
    }
    //*/
    
    
    

    public static Frame frameDivision(Frame x, Frame y) {
        byte[] normal =  x.getRaw();
        byte[] intensified = y.getRaw();

        int len = intensified.length;
        if(normal.length < intensified.length)
            len = normal.length;

        byte[] div = new byte[len];
        for(int i = 0 ; i < len; i++){
            //Increase normal by 1 to avoid zero division
        	if(normal[i]==0){
        		normal[i]+=1;
        	}
        	// Compute Division
            div[i] = (byte)( (intensified[i]/(normal[i]) )*10);
            // Scale resultant intensity by x10
    		
            //Attempt to convert to NV21 byte array to greyscale
            // Not yet working perfectly, image is yellow and very low texture intensity
			if((i)%4 == 0){
				div[i] = (byte)0xFF;
			}
			else{
				if((i)%4==1){
					div[i]=(byte)Math.abs(div[i+2]);
				}
				else if((i)%4==2){
					div[i]=(byte)Math.abs(div[i+1]);
				}
				else if((i)%4==3){
					div[i]=(byte)Math.abs(div[i]);
				}
			}
            
            
        }

        return new Frame(div, x.getWidth(), x.getHeight());
    }
}

