#include <my_global.h>
#include <m_ctype.h>
#include <mysql.h>
#include <m_string.h>
#include <stdio.h>
#include "term.h"
#ifndef GO_PATH
	#error "$GO_PATH was not defined !"
#endif

/*
create function go_isa RETURNS INTEGER SONAME 'go_udf.so';

mysql>  select LEFT(T.name,20) as name,T.acc from go_latest.term as T where go_isa(T.acc,"GO:0016859");
+----------------------+------------+
| name                 | acc        |
+----------------------+------------+
| peptidyl-prolyl cis- | GO:0003755 | 
| retinal isomerase ac | GO:0004744 | 
| maleylacetoacetate i | GO:0016034 | 
| cis-trans isomerase  | GO:0016859 | 
| cis-4-[2-(3-hydroxy) | GO:0018839 | 
| trans-geranyl-CoA is | GO:0034872 | 
| carotenoid isomerase | GO:0046608 | 
| 2-chloro-4-carboxyme | GO:0047466 | 
| 4-hydroxyphenylaceta | GO:0047467 | 
| farnesol 2-isomerase | GO:0047885 | 
| furylfuramide isomer | GO:0047907 | 
| linoleate isomerase  | GO:0050058 | 
| maleate isomerase ac | GO:0050076 | 
| maleylpyruvate isome | GO:0050077 | 
| retinol isomerase ac | GO:0050251 | 
+----------------------+------------+
15 rows in set (1.27 sec)



drop function go_isa;

*/

/* The initialization function */
my_bool go_isa_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
/* The deinitialization function */
void go_isa_deinit(UDF_INIT *initid);
/* The main function. This is where the function result is computed */
long long go_isa(UDF_INIT *initid, UDF_ARGS *args,
              char *is_null, char *error);



static int lower_bound(const TermDBPtr termsdb, const char* name)
	{
	int low = 0;
	int len= termsdb->n_terms;

	while(len>0)
		{
		int half=len/2;
		int mid=low+half;
		if( strncmp(termsdb->terms[mid].child,name,MAX_TERM_LENGTH)<0)
			{
			low=mid;
			++low;
			len=len-half-1;
			}
		else
			{
			len=half;
			}
		}
	return low;
	}


static int termdb_findIndexByName(const TermDBPtr termsdb,const char* name)
	{
	int i=0;
	if(name==NULL || termsdb==NULL || termsdb->terms==NULL || termsdb->n_terms==0) return -1;
	i= lower_bound(termsdb,name);
	if(i<0 || i  >= termsdb->n_terms || strcmp(termsdb->terms[i].child,name)!=0) return -1;
	
	return i;	
	}

static int recursive_search(const TermDBPtr db,int index, const char* parent,int depth)
	{
	int rez=0;
	int start=index;
	int parent_idx=0;
	
	if(start<0 || start>=db->n_terms) return 0;
	
	if(strcmp(db->terms[index].child,parent)==0) return 1;
	while(index < db->n_terms)
		{
		if(strcmp(db->terms[index].child,db->terms[start].child)!=0) break;
		if(strcmp(db->terms[index].parent,parent)==0) return 1;
		parent_idx= termdb_findIndexByName(db,db->terms[index].parent);
		
		rez= recursive_search(db,parent_idx,parent,depth+1);
		if(rez==1 )  return 1;
		++index;
		}
	return 0;
	}


/* The initialization function */
my_bool go_isa_init(
        UDF_INIT *initid,
        UDF_ARGS *args,
        char *message
        )
  {
  TermDBPtr termdb;
  FILE* in=NULL;
  /* check the args */
  if (!(args->arg_count == 2 &&
	args->arg_type[0] == STRING_RESULT &&
	args->arg_type[1] == STRING_RESULT))
    {
    strncpy(message,"Bad parameter expected a DNA",MYSQL_ERRMSG_SIZE);
    return 1;
    }
  initid->maybe_null=1;
  initid->ptr= NULL;

  termdb=(TermDBPtr)malloc(sizeof(TermDB));
  

  if(termdb==NULL)
        {
        strncpy(message,"Out Of Memory",MYSQL_ERRMSG_SIZE);
        return 1;
        }
  
  if((in=fopen(GO_PATH,"r"))==NULL)
        {
        strncpy(message,"Cannot open " GO_PATH ,MYSQL_ERRMSG_SIZE);
	free(termdb);
        return 1;
        }
  if(fread(&(termdb->n_terms),sizeof(int),1,in)!=1)
	{
	strncpy(message,"Cannot read n_items",MYSQL_ERRMSG_SIZE);
	fclose(in);
	free(termdb);
	return 1;
	}
  termdb->terms=malloc(sizeof(Term)*(termdb->n_terms));

	
  if(termdb->terms==NULL)
	{
	strncpy(message,"Cannot alloc terms",MYSQL_ERRMSG_SIZE);
	fclose(in);
	free(termdb);
	return 1;
	}
  if(fread(termdb->terms,sizeof(Term),termdb->n_terms,in)!=termdb->n_terms)
	{
	strncpy(message,"Cannot read items",MYSQL_ERRMSG_SIZE);
	fclose(in);
	free(termdb->terms);
	free(termdb);
	return 1;
	}
  fclose(in);
  initid->ptr=(void*)termdb;
  return 0;
  }

/* The deinitialization function */
void  go_isa_deinit(UDF_INIT *initid)
        {
        /* free the memory **/
        if(initid->ptr!=NULL)
		{
		TermDBPtr termdb=(TermDBPtr)initid->ptr;
		if(termdb->terms!=NULL) free(termdb->terms);
		free(termdb);
		initid->ptr=NULL;
		}
        }


/* The main function. This is where the function result is computed */
long long go_isa(UDF_INIT *initid, UDF_ARGS *args,
              char *is_null, char *error)
 {
  long dnaLength= args->lengths[0];
  const char *child=args->args[0];
  const char *parent=args->args[0];
  char name1[MAX_TERM_LENGTH];
  char name2[MAX_TERM_LENGTH];
  TermDBPtr termdb=(TermDBPtr)initid->ptr;
  int index;

  *is_null=0;

  if (args->args[0]==NULL || args->args[1]==NULL
	|| args->lengths[0]>=MAX_TERM_LENGTH
	|| args->lengths[1]>=MAX_TERM_LENGTH
	) /* Null argument */
   {
    *is_null=1;
    return -1;
   }
  strncpy(name1,args->args[0],args->lengths[0]);
  name1[args->lengths[0]]=0;
  strncpy(name2,args->args[1],args->lengths[1]);	
  name2[args->lengths[1]]=0;

 index=termdb_findIndexByName(termdb,name1);
 if(index==-1)
	{
    	return 0;
	}
 return recursive_search(termdb,index,name2,0);
 }


