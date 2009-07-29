package org.lindenb.treemap;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TreeMapModeler
 *
 */
public class TreeMapModeler
	{
	private static final Comparator<TreeMap> WEIGHT_COMPARE= new Comparator<TreeMap>()
		{
		public int compare(TreeMap o1, TreeMap o2)
			{
			double d1= o1.getWeight();
			double d2= o2.getWeight();
			return (d2==d1 ? 0 : (d1<d2?1:-1));//yes, inverse
			};
		};

   
    public void layout(
    		List<? extends TreeMap> items,
    		Rectangle2D area
    		)
       {
       Collections.sort(items,WEIGHT_COMPARE);
       layout(items,0,items.size(),area);    
       }
    

    private double getWeight(final List<? extends TreeMap> items, int start, int end)
       {
       double sum=0;
       while(start<end)
            {
            sum+=items.get(start++).getWeight();
            }
        return sum;
        }
    
    private void sliceLayout(
            List<? extends TreeMap> items,
            int start, int end,
            Rectangle2D bounds)
        {
        end=Math.min(items.size(),end);
      
        double total = getWeight(items,start, end);
        double a=0; 
        boolean vertical=(bounds.getWidth()<bounds.getHeight() );
        double pos= (vertical?bounds.getY():bounds.getX());
     
        for (int i=start; i<end; i++)
        {
            Rectangle2D.Double  r=new Rectangle2D.Double();
            double b= items.get(i).getWeight()/total;
            if (vertical)
            	{
                r.x= bounds.getX();
                r.width= bounds.getWidth();
                r.y = pos;
                double len = bounds.getHeight()*b;
                r.height = len;
                pos+=(len);
            	}
            else
            	{
                //r.x=(int)(bounds.x+bounds.width*a);
                r.x = pos;
                //r.width=(int)(bounds.width*b);
                double len = bounds.getWidth()*b;
                r.width = len;
                r.y = bounds.getY();
                r.height = bounds.getHeight();
                pos+=(len);
            	}
            
            items.get(i).setBounds(r);
            a+=b;
        }
    }
    
   
    
    private void layout(
    		List<? extends TreeMap> items,
    		int start,
    		int end, 
    		Rectangle2D bounds
    		)
    {
    	end= Math.max(end, items.size());
        if (start>=end) return;
            
        if (end-start<2)
	        {
	        sliceLayout(items,start,end,bounds);
	        return;
	        }
        
        double x=bounds.getX(), y=bounds.getY(), w=bounds.getWidth(), h=bounds.getHeight();
        
        double total=getWeight(items,start, end);
        int mid=start;
        double a= items.get(start).getWeight()/total;
        double b=a;
        
        if (w<h)
        {
            // height/width
            while (mid<end)
            	{
                double aspect=normAspect(h,w,a,b);
                double q= items.get(mid).getWeight()/total;
                if (normAspect(h,w,a,b+q)>aspect) break;
                mid++;
                b+=q;
            	}
         
        	
        sliceLayout(items,start,mid+1,new Rectangle2D.Double(x,y,w,(h*b)));
        layout(items,mid+1,end,new  Rectangle2D.Double(x,(y+h*b),w,(h*(1-b))));
        	
        }
        else
        {
            // width/height
            while (mid<end)
            {
                double aspect=normAspect(w,h,a,b);
                double q= items.get(mid).getWeight()/total;
                if (normAspect(w,h,a,b+q)>aspect) break;
                mid++;
                b+=q;
            }
       
        	
		sliceLayout(items,start,mid+1,new Rectangle2D.Double(x,y,(w*b),h));
		layout(items,mid+1,end,new  Rectangle2D.Double((x+w*b),y,(w*(1-b)),h));
		        
       
        }
        
    }
    
    private static double aspect(double big, double small, double a, double b)
    	{
        return (big*b)/(small*a/b);
    	}
    
    private static double normAspect(double big, double small, double a, double b)
    	{
        double x=aspect(big,small,a,b);
        if (x<1) return 1/x;
        return x;
    	}

	}
