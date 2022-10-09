[//]: # ()
[//]: # (   Copyright \(c\) 2020, Jean-Baptiste BRIAUD. All Rights Reserved.)
[//]: # ()
[//]: # (   Licensed under the Apache License, Version 2.0 \(the "License"\);)
[//]: # (   you may not use this file except in compliance with the License.)
[//]: # (   You may obtain a copy of the License at)
[//]: # ()
[//]: # (       http://www.apache.org/licenses/LICENSE-2.0)
[//]: # ()
[//]: # (   Unless required by applicable law or agreed to in writing, software)
[//]: # (   distributed under the License is distributed on an "AS IS" BASIS,)
[//]: # (   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.)
[//]: # (   See the License for the specific language governing permissions and)
[//]: # (   limitations under the License)
[//]: # ()

<h1 align="center"><b>Ivar</b></h1>
<div align="center">A use case oriented language.</div>

# What is Ivar?
Ivar is a language, a compiler, inspired from Ivar Jacobson **Use Case** concept that ended in UML.  
Ivar is not a generator because it is not needed to modify the outputs procuced by the compiler, unlike a generator.  
It is not needed, but still possible. That's why we make a point to ensure ivar compiler outcomes are humman readable.  
So, yes, it is possible to modify such Ivar output files, but it is not advised because on the next compilation, modifications will be lost.  
Also, there is no need to modify files to make the *targapp* work. The resulting web application, the *targapp*, is deployable rigth baked by the compiler.  
The compiler produce the Ant file that handle full Targapp lifecycle, including Tomcat deployment.
# Why a new language?
As a developer, I barely seen IT approach applyied to IT itself. Ivar is a try to do this with a new language that produce what is currently written by hand for a web application that manipulate "business data": HTML or GUI, a business logic server and a relational database.

Language approach is very powerful: a text input file and something that can run as an output.
# Maturity
Current Ivar iteration is not mature to start using Ivar language right now (Sept 2022).  
TODO:
- Ivar Grammar is incomplete and is closer to a test than a complete language.
- Qooxdoo template use very old version of Qooxdoo and need to be upgraded.  

Note that the compiler is far more advanced than grammar. Yeah ... I know ... strange but true due to the specific history of thie compiler ;-)

Once that will be done, one can start usinbg Ivar to develop a CRUD business data web application.
# Definitions
- _targapp_ is an application defined in Ivar language and produce by ivarc, the Ivar language compiler.
- _targapp arch_ is the set of tech, the software architecture, that underlie targapp. For now, there is only one possible targapp arch.
- _ivarc_ is the command line compiler. It take an ivar file and produce an application.
# Targapp arch
This paragraph will introduce, without details, the software architecture that underlie a targapp.
Currently, there is one possible targapp arch, but ivar had been designed with template to paremetrize the generation so it can evolve.
This is done using Apache Velocity powerful template engine, but this is part of ivar compiler software architecture.
There is no name for this only possible targapp software architecture.

- Web, single page front-end based on Qooxdoo javascript framework.
- Java back-end
- JSON to Java instance and, back, Java instance to JSON serialization
- OpenJPA persistence based on JDBC compatible relational database (MySQL, PostgreSQL, SQLServer, ...)

# Versioning
https://semver.org/

# Contributing. Can I help?
Sure you can!  
Don't be shy. You can contribute to a programming language, even if you start programming right now. You will learn as all other developers did before you.  
Currently, there would a lot of possibilities to help:
- upgrade Qooxdoo template.
- new template for other tech to target.
# Ivar compiler high level software architecture
Ivar compiler is developed in Java.
The front-end is a command line compiler, ivarc.
The ivar file source code is lex, parsed and visit using SableCC generated classes.
This build a tree of ivar metamodel concepts which is not directly the AST but is build from the AST.
In such tree we'll find Application, Scenario, Step, etc.
This ivar metamodel tree instance is about the same level of abstraction than the Ivar language itself. It is very often more verbose and less dense than the language itself.
Then, an application compiler is launched and, from this ivar metamodel tree, will build another tree of technical generic concepts like Screen, BusinessObject, etc.
Then, in a next phase, the compiler will build another tree of more low level technical concepts that totally depends on the targapp arch, there we'll find QooxdooScreen, JavaBusinessObject, etc.
Then, this latest tree if browse by generators to produce Qooxdoo javascript files and JPA annotated Java files for example.

# Ivar source code organization
The purpose of this paragraph is to document the organization of the Ivar project Java source code.
It is developped with Netbeans but you can use any IDE.
The global ivar root folder contains projects. Each one has dependencies : with other projects and with libraries.
Definition: 
- _leaf project_ doesn't depends on other projects.

## /ivar-grammar
This project is a leaf and ha no IDE dependencies.
This project contain the SableCC definition of Ivar language and the SableCC generated files for lexer, parser and AST visitor.
SableCC is needed to compale the Ivar grammar into lexer, parser and visitor.
This is launched by an Ant script that need SableCC lib.
## /helper
This project is a leaf. It contains utility classes, helper.
Most methods should be static.
### Dependencies
- project
- lib
  - Apache commons
## /common
This project is shared by ivar compiler and targapp runtime.
## /ivar-metamodel
This project is the Java code for all Ivar compiler concepts and phases.
- Classes from phase 1: Application, Scenario, Step, etc.
- Classes from the generic phase: Screen, ScreenField, BusinessObject, etc.
- Classed from the target phase: QooxdooScreen, JavaBusinessObject, etc.
### Dependencies
- project
  - helper
  - common
## /jfwk
This project is the home for targapp @compiletime and @runtime classes.
It is not used by the ivar compiler.
### Dependencies
- standard
  - JEE
- project
  - helper
  - common
  - lib-rt-json
  - lib-rt-serialization
  - lib-rt-rpc
- lib
  - Apache OpenJPA
  - Apache Commons
  - Apache Velocity
## /ivar-compiler
### Dependencies
- project
  - helper
  - common
  - ivar-metamodel
  - ivar-grammar
- lib
  - Apache Velocity
  - args4j
  - Apache Commons
  - SableCC
  - scriptonite
## /lib-rt-json
Source code from json.org
No dependencies.
## /lib-rt-serialization
Source code to serialize from Java instance to JSON and from JSON to Java instance.
### Dependencies
- project
  - lib-rt-json
## /lib-rt-rpc
A global RPC (Remote ProcedureCall) based on JSON over HTTP.
### Dependencies
- project
  - lib-rt-json
  - lib-rt-serialization
## /qxfwj
This project contains the runtime Javascript framework for targapp.
It is not used by ivar compiler.
### Dependencies
It depends on Qooxdoo framework.





- This links to [a different section on the same page, using a "#" and the header ID](#header-ids-and-links)
