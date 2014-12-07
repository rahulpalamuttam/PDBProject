ARG1='$1'

UNLABELED_SET=/Users/rahulpalamuttam/Research_UCSD_Protein/UNLABELED_DATASET/UNLABELED_NXML_FILES
TARFILE=/Users/rahulpalamuttam/Research_UCSD_Protein/articles.A-B.tar

NXML_FILES=/Users/rahulpalamuttam/Research_UCSD_Protein/POSITIVE_DATASET/NXML_FILES/
UNRELEASED_SET=unreleasedPDBid.csv

TARGET_JAR=target/pdbproj-1.0-SNAPSHOT.jar
SPARK_HOME=~/spark-1.1.0/bin
$SPARK_HOME/spark-submit --class org.sdsc.pdbproject.Main $TARGET_JAR $NXML_FILES $UNRELEASED_SET
SUCCESS=$?
DATE=$(date)
if(($SUCCESS == 0))
then
    echo "Doing stuffs"
    scp target/pdbproj-1.0-SNAPSHOT.jar rpalamut@gordon.sdsc.edu:
fi


