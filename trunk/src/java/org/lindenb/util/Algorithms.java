package org.lindenb.util;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Algorith used to search in an orderer collection of Vector of type T
 * comparaison is done on K which can be extracted from T
 * @author lindenb
 *
 * @param <T> vector type
 * @param <K> the key used for sorting to extract from T and to compare
 */
public abstract class Algorithms<T,K>
	{
	private Comparator<K> comparator;

	/**
	 * Algorithms
	 * @param comparator used to compare two keys of type K
	 */
	public Algorithms(Comparator<K> comparator)
		{
		this.comparator=comparator;
		}
	
	/** @return the internal comparator */
	public Comparator<K> getComparator()
		{
		return this.comparator;
		}
	
	/** method used to extract the key(K) from an object (T) */
	public abstract K getKey(T value);
	
	
	/** @return wether the vector is sorted */
	public boolean isSorted(List<T> dataVector)
		{
		return isSorted(dataVector,0,dataVector.size());
		}
	
	/** @return wether the vector is sorted between begin and end*/
	public boolean isSorted(List<T> dataVector,int begin,int end)
		{
		while(begin+1< end)
			{
			if(getComparator().compare(getKey(dataVector.get(begin)),getKey(dataVector.get(begin+1)))>0)
				{
				return false;
				}
			++begin;
			}
		return true;
		}
	
	/** C+ equals range */
	public Pair<Integer, Integer> equal_range(
			List<T> dataVector,
	        K select
	        )
		{
		return equal_range(dataVector,0,dataVector.size(), select);
		}
	
	/** C+ equals range 
	 * 
	 * @param bounds array of two integer [begin; end[ 
	 * @param select the value to search
	 * */
	public Pair<Integer, Integer> equal_range(
			List<T> dataVector,
			Pair<Integer,Integer> bounds,
	        K select
	        )
		{
		return equal_range(dataVector,bounds.first(),bounds.second(), select);
		}
	
	/** C+ equals range */
	public Pair<Integer, Integer> equal_range(
			List<T> dataVector,
			int first,
			int last,
            K subject
            )
        {
		int left= lower_bound(dataVector, first,last,subject);
		int right= upper_bound(dataVector, left,last,subject);
		return new Pair<Integer,Integer>(left,right);
        }
	
/** C+ lower_bound */
public int lower_bound(
		List<T> dataVector,
        K select
        )
	{
	return lower_bound(dataVector,0,dataVector.size(), select);
	}

/** C+ lower_bound */
public  int lower_bound(
			List<T> dataVector,
			int first, int last,
            K select
            )
    {
    int len = last - first;
    while (len > 0)
            {
            int half = len / 2;
            int middle = first + half;
            T x= dataVector.get(middle);
            if (getComparator().compare(getKey(x),select)<0)
                    {
                    first = middle + 1;
                    len -= half + 1;
                    }
            else
                    {
                    len = half;
                    }
            }
    return first;
    }

/** C+ upper_bound */
public int upper_bound(
		Vector<T> dataVector,
        K select
        )
	{
	return upper_bound(dataVector,0,dataVector.size(), select);
	}


/** C+ upper_bound */
public  int upper_bound(
		List<T> dataVector,
		int first, int last,
        K select
        )
    {
    int len = last - first;
    while (len > 0)
            {
            int half = len / 2;
            int middle = first + half;
            T x= dataVector.get(middle);
            if (getComparator().compare(select,getKey(x))<0)
                    {
                    len = half;
                    }
            else
                    {
                    first = middle + 1;
                    len -= half + 1;
                    }
            }
    return first;
    }

@Override
public String toString() {
	return "Algorithm("+getComparator()+")";
	}
}
