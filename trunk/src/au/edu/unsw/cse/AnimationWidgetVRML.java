/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of ModelGUI[core] (mgui-core).
* 
* ModelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.unsw.cse;


public class AnimationWidgetVRML extends AnimationWidget{ 

  public AnimationWidgetVRML(Viewer viewer){
    super(viewer);
  }

  
  public void start(){
  }
  
  public void stop(){
  }


  public void run() {
    // running 
  }

  public void next() {
  }
  	
  public void prev() {
  }
  	
  public static void toVRML(VRMLState state){
    state.append(
      "PROTO SquareTexture [\n"+
      "  field MFString textureName \"ButtonPlay.jpg\"\n"+
      "  eventIn SFColor set_diffuseColor\n"+
      "]{\n"+
      "  Shape {\n"+
      "    appearance Appearance {\n"+
      "      texture ImageTexture {\n"+
      "	url IS textureName\n"+
      "      }\n"+
      "      material Material{\n"+
      "	set_diffuseColor IS set_diffuseColor\n"+
      "	diffuseColor 0.5 0.5 0.5\n"+
      "      }\n"+
      "    }\n"+
      "    geometry IndexedFaceSet{\n"+
      "      coord Coordinate {\n"+
      "	point [0.5 0.5 -0.001, 0.5 -0.5 -0.001, -0.5 -0.5 -0.001, -0.5 0.5 -0.001]\n"+
      "      }\n"+
      "      coordIndex [0, 3, 2, 1, -1]\n"+
      "    }\n"+
      "  }\n"+
      "}\n"+
      "\n"+
      "PROTO CheckBox [\n"+
      "  field SFString description \"Click me\"\n"+
      "  field MFString textureName \"normal.png\"\n"+
      "  field MFString textureName2 \"normal2.png\"\n"+
      "  eventOut SFFloat transparency\n"+
      "  eventOut SFInt32 choice\n"+
      "  field SFBool state TRUE\n"+
      "] {\n"+
      "  Transform{\n"+
      "    children[\n"+
      "      DEF TEXTURESWITCH Switch {\n"+
      "	whichChoice 0\n"+
      "	choice [\n"+
      "	  DEF MAT1 SquareTexture{\n"+
      "	    textureName IS textureName\n"+
      "	  }\n"+
      "	  DEF MAT2 SquareTexture{\n"+
      "	    textureName IS textureName2\n"+
      "	  }\n"+
      "	]\n"+
      "      }\n"+
      "      DEF TOUCHER TouchSensor {}\n"+
      "    ]	\n"+
      "  }\n"+
      "  DEF HIGHLIGHTER Script {\n"+
      "    eventIn SFBool isActive\n"+
      "    eventIn SFTime click\n"+
      "    eventOut SFInt32 choice IS choice\n"+
      "    field SFBool state TRUE\n"+
      "    eventOut SFFloat transparency IS transparency\n"+
      "    field SFString description IS description\n"+
      "    eventOut SFColor color_changed\n"+
      "    field SFColor color 0.5 0.5 0.5\n"+
      "    field SFColor high_color 0.8 0.8 0.8\n"+
      "    url [\n"+
      "      \"vrmlscript:\n"+
      "        function isActive(eventValue) {\n"+
      "          if (eventValue){\n"+
      "             Browser.setDescription((state?'Hide ':'Show ')+description);\n"+
      "             color_changed = high_color;\n"+
      "          } else {\n"+
      "             Browser.setDescription('');\n"+
      "             color_changed = color;\n"+
      "          }\n"+
      "        }\n"+
      "        \n"+
      "        function click(value) {\n"+
      "          state = !state;\n"+
      "          isActive(true);\n"+
      "          transparency = state ? 0 : 1;\n"+
      "          choice = state ? 0 : 1;\n"+
      "        }\"\n"+
      "    ]\n"+
      "  }\n"+
      "  ROUTE TOUCHER.isOver TO HIGHLIGHTER.isActive\n"+
      "  ROUTE TOUCHER.touchTime TO HIGHLIGHTER.click\n"+
      "  ROUTE HIGHLIGHTER.choice TO TEXTURESWITCH.set_whichChoice\n"+
      "  ROUTE HIGHLIGHTER.color_changed TO MAT1.set_diffuseColor\n"+
      "  ROUTE HIGHLIGHTER.color_changed TO MAT2.set_diffuseColor\n"+
      "}\n"+
      "\n"+
      "PROTO HighLighter [\n"+
      "  eventIn SFBool isActive\n"+
      "  eventOut SFColor color_changed\n"+
      "  field SFString description \"\"\n"+
      "  field SFColor color 0.5 0.5 0.5\n"+
      "  field SFColor high_color 0.8 0.8 0.8\n"+
      "] {\n"+
      "  Script {\n"+
      "    eventIn SFBool isActive IS isActive\n"+
      "    eventOut SFColor color_changed IS color_changed\n"+
      "    field SFString description IS description\n"+
      "    field SFColor color IS color\n"+
      "    field SFColor high_color IS high_color\n"+
      "    url [\n"+
      "      \"vrmlscript:\n"+
      "        function isActive(eventValue) {\n"+
      "          if (eventValue == true){\n"+
      "             Browser.setDescription(description);\n"+
      "             color_changed = high_color;\n"+
      "          } else {\n"+
      "             Browser.setDescription('');\n"+
      "             color_changed = color;\n"+
      "          }\n"+
      "        }\"\n"+
      "    ]\n"+
      "  }\n"+
      "}\n"+
      "\n"+
      "PROTO Button [\n"+
      "  field SFString description \"Click me\"\n"+
      "  field MFString textureName []\n"+
      "  eventOut SFTime touchTime\n"+
      "] {\n"+
      "  Transform{\n"+
      "    children[\n"+
      "      DEF MAT SquareTexture {\n"+
      "	textureName IS textureName\n"+
      "      }\n"+
      "      DEF TOUCHER TouchSensor {touchTime IS touchTime}\n"+
      "    ]\n"+
      "  }\n"+
      "  DEF HIGHLIGHTER HighLighter {\n"+
      "    description IS description\n"+
      "  }\n"+
      "  ROUTE TOUCHER.isOver TO HIGHLIGHTER.isActive\n"+
      "  ROUTE HIGHLIGHTER.color_changed TO MAT.set_diffuseColor\n"+
      "}\n"+
      "#Heads Up Display based on code by Chris Fouts\n"+
      "PROTO Slider [\n"+
      "  field SFVec3f translation 0 0 0\n"+
      "  field SFColor color 0.5 0.5 0.5\n"+
      "  field SFColor high_color 1 1 1\n"+
      "  field MFVec3f point []\n"+
      "  field SFVec3f offset 0 0 0\n"+
      "  field SFString description \"\"\n"+
      "  eventIn SFVec3f set_offset\n"+
      "  eventIn SFVec3f set_translation\n"+
      "  eventOut SFVec3f translation_changed\n"+
      "] {\n"+
      "  Transform {\n"+
      "    translation IS translation\n"+
      "    children [\n"+
      "      DEF TOUCHER TouchSensor {}\n"+
      "      PlaneSensor {\n"+
      "	minPosition 0 0\n"+
      "	maxPosition 1 0\n"+
      "	set_offset IS set_offset\n"+
      "	offset IS offset\n"+
      "	translation_changed IS translation_changed\n"+
      "      }\n"+
      "      Transform {\n"+
      "        translation IS offset\n"+
      "	set_translation IS set_translation\n"+
      "	children [\n"+
      "	  \n"+
      "	  Shape {\n"+
      "	    appearance Appearance {\n"+
      "	      material DEF MAT Material {\n"+
      "		diffuseColor 0 0 0\n"+
      "		emissiveColor IS color\n"+
      "	      }\n"+
      "	    }\n"+
      "	    geometry IndexedFaceSet {\n"+
      "	      solid FALSE\n"+
      "	      coord DEF Coord Coordinate {\n"+
      "		point IS point\n"+
      "	      }\n"+
      "	      coordIndex [ 0 1 2 3 -1 ]\n"+
      "	    }\n"+
      "	  }\n"+
      "	  \n"+
      "	  Shape {\n"+
      "	    appearance Appearance {\n"+
      "	      material Material {\n"+
      "		emissiveColor .2 .2 .2\n"+
      "	      }\n"+
      "	    }\n"+
      "	    geometry IndexedLineSet {\n"+
      "	      coord USE Coord \n"+
      "	      coordIndex [ 0 1 2 3 0  -1 ]\n"+
      "	    }\n"+
      "	  }\n"+
      "	  \n"+
      "	]\n"+
      "      }\n"+
      "    ]\n"+
      "  }\n"+
      "  DEF HIGHLIGHTER HighLighter {\n"+
      "    description IS description\n"+
      "    color IS color\n"+
      "    high_color IS high_color\n"+
      "  }\n"+
      "  ROUTE TOUCHER.isOver TO HIGHLIGHTER.isActive\n"+
      "  ROUTE HIGHLIGHTER.color_changed TO MAT.set_emissiveColor\n"+
      "}\n"+
      "DEF HudGroup Collision {\n"+
      "  collide FALSE\n"+
      "  children [\n"+
      "    DEF HudProx ProximitySensor {\n"+
      "      size 200 200 200\n"+
      "    }\n"+
      "    DEF HudAdjustProx ProximitySensor {\n"+
      "      size 10 10 10\n"+
      "    }\n"+
      "    DEF HudXform Transform {\n"+
      "      children [\n"+
      "	DEF HudAdjust Transform{\n"+
      "	  translation -0.06 -0.07 -0.15\n"+
      "	  scale 0.01 0.01 0.01\n"+
      "	  children[\n"+
      "	    Transform{\n"+
      "	      translation -0.8 -0.2 0\n"+
      "	      scale 0.5 0.5 1\n"+
      "	      children[\n"+
      "		DEF CONTROLS CheckBox {\n"+
      "		  description \"control panel\"\n"+
      "		  textureName \"down.png\"\n"+
      "		  textureName2 \"up.png\"\n"+
      "		}\n"+
      "	      ]\n"+
      "	    }\n"+
      "	    DEF CONTROLSWITCH Switch{\n"+
      "	      whichChoice 0\n"+
      "	      choice [\n"+
      "		Group {\n"+
      "		  children [\n"+
      "		    Transform{\n"+
      "		      translation 0 0.35 0\n"+
      "		      children[\n"+
      "			DEF START Button {\n"+
      "			  description \"Start playing the animation\"\n"+
      "			  textureName \"play.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 1 0.35 0\n"+
      "		      children[\n"+
      "			DEF LOOP Button {\n"+
      "			  description \"Play the animation and keep repeating\"\n"+
      "			  textureName \"loop.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 2 0.35 0\n"+
      "		      children[\n"+
      "			DEF STOP Button {\n"+
      "			  description \"Stop playing the animation\"\n"+
      "			  textureName \"stop.png\"	\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 3 0.35 0\n"+
      "		      children[\n"+
      "			DEF NEXT Button {\n"+
      "			  description \"Go forward one frame\"\n"+
      "			  textureName \"step.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 4 0.35 0\n"+
      "		      children[\n"+
      "			DEF PREV Button {\n"+
      "			  description \"Go back one frame\"\n"+
      "			  textureName \"stepback.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 5 0.35 0\n"+
      "		      children[\n"+
      "			DEF FIRST Button {\n"+
      "			  description \"Go to the beginning of the animation\"\n"+
      "			  textureName \"first.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 6 0.35 0\n"+
      "		      children[\n"+
      "			DEF LAST Button {\n"+
      "			  description \"Go to the end of the animation\"\n"+
      "			  textureName \"last.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    Transform{\n"+
      "		      translation 7 0.35 0\n"+
      "		      children[\n"+
      "			Shape{\n"+
      "			  geometry DEF COUNTERDISPLAY Text {	\n"+
      "			    string \"0\"\n"+
      "			    fontStyle FontStyle {\n"+
      "			      justify [\"LEFT\", \"MIDDLE\"]\n"+
      "			    }\n"+
      "			  }\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n");
      state.getViewer().toVRML(state);
      state.append(
      "		    Transform {\n"+
      "		      translation 3.15 -.35 0\n"+
      "		      scale 7 3 1\n"+
      "		      children [\n"+
      "			DEF MinSlider Slider{\n"+
      "			  description \"Set first frame\"\n"+
      "			  translation -.51 0 0\n"+
      "			  color .1 .6 .1\n"+
      "			  high_color .2 1 .2\n"+
      "			  point [\n"+
      "			    .01 0 .001,\n"+
      "			    -.01 -.05 .001,\n"+
      "			    -.01 .05 .001,\n"+
      "			    -.01 .05 .001,\n"+
      "			  ]\n"+
      "			}\n"+
      "			DEF MaxSlider Slider{\n"+
      "			  description \"Set last frame\"\n"+
      "			  translation -.49 0 0\n"+
      "			  color .6 .1 .1\n"+
      "			  high_color 1 .2 .2\n"+
      "			  point [\n"+
      "			    -.01 0 .001,\n"+
      "			    .01 -.05 .001,\n"+
      "			    .01 .05 .001,\n"+
      "			    .01 .05 .001\n"+
      "			  ]\n"+
      "			  offset 1 0 0\n"+
      "			}\n"+
      "			\n"+
      "			DEF FrameSlider Slider{\n"+
      "			  description \"Set frame\"\n"+
      "			  translation -.5 0 0\n"+
      "			  color .1 .1 .5\n"+
      "			  high_color .2 .2 .9\n"+
      "			  point [\n"+
      "			    -.005 -.05 .002,\n"+
      "			    .005 -.05 .002,\n"+
      "			    .005 .05 .002,\n"+
      "			    -.005 .05 .002  \n"+
      "			  ]\n"+
      "			}\n"+
      "			\n"+
      "			Transform {\n"+
      "			  scale 1.04 1 1\n"+
      "			  children [\n"+
      "			    Shape {\n"+
      "			      appearance Appearance {\n"+
      "				material Material {\n"+
      "				  diffuseColor .5 .5 .5\n"+
      "				}\n"+
      "			      }\n"+
      "			      geometry IndexedFaceSet {\n"+
      "				coord DEF IndCoords Coordinate { \n"+
      "				  point [\n"+
      "				    -.5 -.05 0  .5 -.05 0  .5 .05 0  -.5 .05 0\n"+
      "				  ]\n"+
      "				}\n"+
      "				coordIndex [0 1 2 3 -1]\n"+
      "			      }\n"+
      "			    }\n"+
      "			    Shape {\n"+
      "			      appearance Appearance {\n"+
      "				material Material {\n"+
      "				  emissiveColor .2 .2 .2\n"+
      "				}\n"+
      "			      }\n"+
      "			      geometry IndexedLineSet {\n"+
      "				coord USE IndCoords\n"+
      "				coordIndex [0 1 2 3 0 -1]\n"+
      "			      }\n"+
      "			    }\n"+
      "			    \n"+
      "			  ]\n"+
      "			}\n"+
      "			\n"+
      "		      ]\n"+
      "		    }\n"+
      "		    \n"+
      "		  ]\n"+
      "		}\n"+
      "	      ]\n"+
      "	    }\n"+
      "	  ]\n"+
      "	}\n"+
      "      ]\n"+
      "    }\n"+
      "  ]\n"+
      "  \n"+
      "  ROUTE CONTROLS.choice TO CONTROLSWITCH.set_whichChoice\n"+
      "  ROUTE HudProx.orientation_changed  TO HudXform.rotation\n"+
      "  ROUTE HudProx.position_changed  TO HudXform.translation\n"+
      "}\n"+
      "\n"+
      "DEF HudAdjuster Script {\n"+
      "  eventIn SFBool isActive\n"+
      "  eventOut SFVec3f translation_changed\n"+
      "  eventOut SFVec3f scale_changed\n"+
      "    url [\n"+
      "      \"vrmlscript:\n"+
      "        function isActive(eventValue) {\n"+
      "          if (eventValue == true){\n"+
      "             translation_changed[0] = -0.06;\n"+
      "             translation_changed[1] = -0.07;\n"+
      "             translation_changed[2] = -0.15;\n"+
      "             scale_changed[0] = 0.01;\n"+
      "             scale_changed[1] = 0.01;\n"+
      "             scale_changed[2] = 0.01;\n"+
      "          } else {\n"+
      "             translation_changed[0] = -0.0012;\n"+
      "             translation_changed[1] = -0.0014;\n"+
      "             translation_changed[2] = -0.15;\n"+
      "             scale_changed[0] = 0.0002;\n"+
      "             scale_changed[1] = 0.0002;\n"+
      "             scale_changed[2] = 0.0002;\n"+
      "          }\n"+
      "        }\"\n"+
      "    ]\n"+
      "}\n"+
      "\n"+
      "ROUTE HudAdjuster.translation_changed TO HudAdjust.set_translation\n"+
      "ROUTE HudAdjuster.scale_changed TO HudAdjust.set_scale\n"+
      "ROUTE HudAdjustProx.isActive TO HudAdjuster.isActive\n"+
      "\n"+
      "DEF Clock TimeSensor {\n"+
      "	cycleInterval 100000\n"+
      "	loop TRUE\n"+
      "	startTime 1.0\n"+
      "	stopTime 0.0\n"+
      "}\n"+
      "\n"+
      "DEF Animator Script {\n"+
      "  directOutput TRUE\n"+
      "\n"+
      "  eventIn  SFFloat set_first \n"+
      "  eventIn  SFFloat set_last \n"+
      "  eventIn  SFFloat set_fraction \n"+
      "\n"+
      "  eventIn SFTime tick\n"+
      "  eventIn SFBool isActive\n"+
      "  eventIn SFTime start\n"+
      "  eventIn SFTime loopstart\n"+
      "  eventIn SFTime stop\n"+
      "  eventIn SFTime next\n"+
      "  eventIn SFTime prev\n"+
      "  eventIn SFTime gofirst\n"+
      "  eventIn SFTime golast\n"+
      "\n"+
      "  eventOut SFFloat time_changed\n"+
      "  eventOut SFTime startTime\n"+
      "  eventOut SFTime stopTime\n"+
      "  eventOut MFString frameCounter\n"+
      "  eventOut SFFloat fraction_changed\n"+
      "\n"+
      "  field SFFloat time 0\n"+
      "  field SFTime tn1 0       # Last tick's time value\n"+
      "  field SFBool isfirst TRUE  # This is the first tick\n"+
      "  field SFBool backwards FALSE # is time running backwards?\n"+
      "  field SFBool loop FALSE # should we start again when we get to the end?\n"+
      "  field SFTime firstFrame 0\n"+
      "  field SFTime lastFrame " + state.getLastFrame() + "\n"+
      "  field SFTime cycleInterval " + state.getLastFrame() + "\n"+
      "  field SFInt32 prevFrame 0\n"+
      "\n"+
      "  field SFInt32 selectChoice 3  # choice to use for selected things \n"+
      "  field SFInt32 deleteChoice 2  # choice to use for things about to be\n"+
      "                                # deleted\n"+
      "  field SFInt32 addChoice 1  # choice to use for things about to be\n"+
      "                                # added\n"+
      "  field SFInt32 regularChoice 0 # choice to use for regular things\n"+
      "\n"+
    "  field MFNode appear [");
    Object3dList appear = state.getAppear();
    appear.sortByFirstFrame();
    state.append("USE "+appear.elementAt(0).id());//dummy value
    for (int i = appear.size()-1; i>= 0; i--){
       state.append(", USE "+appear.elementAt(i).id());
    }
    state.append("]\n"+
    "  field MFInt32 appearTime[-1,");
    for (int i = appear.size()-1; i>= 0; i--){
       state.append(appear.elementAt(i).getFirstFrame()+",");
    }
    state.append("99999]\n"+
    "  field SFInt32 appearIndex 1\n"+
    "  field MFNode disappear [");
    Object3dList disappear = state.getDisappear();
    disappear.sortByLastFrame();
    state.append("USE "+appear.elementAt(0).id());//dummy value
    for (int i = disappear.size()-1; i>= 0; i--){
       state.append(", USE "+disappear.elementAt(i).id());
    }
    state.append("]\n"+
    "  field MFInt32 disappearTime[-1,");
    for (int i = disappear.size()-1; i>= 0; i--){
       state.append(disappear.elementAt(i).getLastFrame()+",");
    }
    state.append("99999]\n"+
      "  field SFInt32 disappearIndex 1\n"+
    "  field MFNode select [");
    Object3dList select = state.getSelect();
    select.sortBySelectFrame();
    state.append("USE "+appear.elementAt(0).id());//dummy value
    for (int i = select.size()-1; i>= 0; i--){
       state.append(", USE "+select.elementAt(i).id());
    }
    state.append("]\n"+
    "  field MFInt32 selectTime[-1,");
    for (int i = select.size()-1; i>= 0; i--){
       state.append(select.elementAt(i).getSelectFrame()+",");
    }
    state.append("99999]\n"+
    "  field SFInt32 selectIndex 1\n"+
      "      url [\"vrmlscript:\n"+
      "\n"+
      "        function isActive(active) {\n"+
      "          isfirst = true;\n"+
      "        }\n"+
      "\n"+
      "	function tick(value) {\n"+
      "	  if (isfirst) {\n"+
      "	    isfirst = false;\n"+
      "	    tn1 = value;\n"+
      "	  } else {\n"+
      "            if (backwards) {\n"+
      "	      time = time - (value - tn1);\n"+
      "            } else {\n"+
      "	      time = time + value - tn1;\n"+
      "            }\n"+
      "	    tn1 = value;\n"+
      "	  }\n"+
      "          setTime(value);\n"+
      "        }\n"+
      "\n"+
      "        function setTime(value) {\n"+
      "          if (time>lastFrame) {\n"+
      "            if (loop) {\n"+
      "              time = time - lastFrame + firstFrame;\n"+
      "            } else {\n"+
      "              time = lastFrame;\n"+
      "              stop(value);\n"+
      "            }\n"+
      "          }\n"+
      "          if (time<firstFrame) {\n"+
      "            time = firstFrame;\n"+
      "            stop(value);\n"+
      "          }\n"+
      "          thisFrame = Math.floor(time);\n"+
      "          frameCounter[0] = String(thisFrame);\n"+
      "          if (thisFrame > prevFrame) {\n"+
      "            while(appearTime[appearIndex-1]>=prevFrame) {\n"+
      "              appearIndex--;\n"+
      "            }\n"+
      "            for(;appearTime[appearIndex] < thisFrame; appearIndex++){\n"+
      "              appear[appearIndex].set_whichChoice = regularChoice;\n"+
      "            }\n"+
      "            while(selectTime[selectIndex-1]>=prevFrame) {\n"+
      "              selectIndex--;\n"+
      "            }\n"+
      "            for(;selectTime[selectIndex] < thisFrame; selectIndex++){\n"+
      "              select[selectIndex].set_whichChoice = regularChoice;\n"+
      "            }\n"+
      "            while(disappearTime[disappearIndex-1]>=prevFrame) {\n"+
      "              disappearIndex--;\n"+
      "            }\n"+
      "            for(;disappearTime[disappearIndex] < thisFrame; disappearIndex++){\n"+
      "              disappear[disappearIndex].set_whichChoice = -1;\n"+
      "            }\n"+
      "            for(;disappearTime[disappearIndex] <= thisFrame; disappearIndex++){\n"+
      "              disappear[disappearIndex].set_whichChoice = deleteChoice;\n"+
      "            }\n"+
      "            for(;appearTime[appearIndex] <= thisFrame; appearIndex++){\n"+
      "              appear[appearIndex].set_whichChoice = addChoice;\n"+
      "            }\n"+
      "            for(;selectTime[selectIndex] <= thisFrame; selectIndex++){\n"+
      "              select[selectIndex].set_whichChoice = selectChoice;\n"+
      "            }\n"+
      "          } else if (thisFrame < prevFrame) {\n"+
      "            for(;selectTime[selectIndex-1] > thisFrame; selectIndex--){\n"+
      "              select[selectIndex-1].set_whichChoice = regularChoice;\n"+
      "            }\n"+
      "            for(;disappearTime[disappearIndex-1] > thisFrame; disappearIndex--){\n"+
      "              disappear[disappearIndex-1].set_whichChoice = regularChoice;\n"+
      "            }\n"+
      "            for(;disappearTime[disappearIndex-1] >= thisFrame; disappearIndex--){\n"+
      "              disappear[disappearIndex-1].set_whichChoice = deleteChoice;\n"+
      "            }\n"+
      "            while(disappearTime[disappearIndex]<=thisFrame) {\n"+
      "              disappearIndex++;\n"+
      "            }\n"+
      "            for(;appearTime[appearIndex-1] > thisFrame; appearIndex--){\n"+
      "              appear[appearIndex-1].set_whichChoice = -1;\n"+
      "            }\n"+
      "            for(;appearTime[appearIndex-1] >= thisFrame; appearIndex--){\n"+
      "              appear[appearIndex-1].set_whichChoice = addChoice;\n"+
      "            }\n"+
      "            while(appearTime[appearIndex]<=thisFrame) {\n"+
      "              appearIndex++;\n"+
      "            }\n"+
      "            for(;selectTime[selectIndex-1] >= thisFrame; selectIndex--){\n"+
      "              select[selectIndex-1].set_whichChoice = selectChoice;\n"+
      "            }\n"+
      "            while(selectTime[selectIndex]<=thisFrame) {\n"+
      "              selectIndex++;\n"+
      "            }\n"+
      "          }\n"+
      "          prevFrame = thisFrame;\n"+
      "          time_changed = time;\n"+
      "          fraction_changed = time / cycleInterval;\n"+
      "	}\n"+
      "\n"+
      "        function set_fraction(value,timeStamp){\n"+
      "          stop(timeStamp);\n"+
      "          time = cycleInterval*value;\n"+
      "          setTime(value);\n"+
      "        }\n"+
      "\n"+
      "        function start(value) {\n"+
      "          loop = false;\n"+
      "          backwards = false;\n"+
      "          startTime = value;\n"+
      "        }\n"+
      "\n"+
      "        function loopstart(value) {\n"+
      "          loop = true;\n"+
      "          backwards = false;\n"+
      "          startTime = value;\n"+
      "        }\n"+
      "\n"+
      "        function stop(value) {\n"+
      "          stopTime = value;\n"+
      "        }\n"+
      "\n"+
      "        function next(value) {\n"+
      "          backwards = false;\n"+
      "          startTime = value;\n"+
      "          stopTime = value+1;\n"+
      "        }\n"+
      "\n"+
      "        function prev(value) {\n"+
      "          backwards = true;\n"+
      "          startTime = value;\n"+
      "          stopTime = value+1;\n"+
      "        }\n"+
      "\n"+
      "        function gofirst(value) {\n"+
      "          stop(value);\n"+
      "          time = firstFrame;\n"+
      "          setTime(value);\n"+
      "        }\n"+
      "\n"+
      "        function golast(value) {\n"+
      "          stop(value);\n"+
      "          time = lastFrame;\n"+
      "          setTime(value);\n"+
      "        }\n"+
      "\n"+
      "        function set_first(value){\n"+
      "          firstFrame = cycleInterval*value;\n"+
      "        }\n"+
      "\n"+
      "        function set_last(value){\n"+
      "          lastFrame = cycleInterval*value;\n"+
      "        }\n"+
      "\n"+
      "      \"]\n"+
      "}\n"+
      "  DEF Indicator Script {\n"+
      "    eventIn  SFFloat set_fraction\n"+
      "    eventOut SFFloat fraction_changed\n"+
      "    eventOut SFFloat min_changed\n"+
      "    eventOut SFFloat max_changed\n"+
      "    eventIn  SFVec3f set_translation\n"+
      "    eventOut SFVec3f translation_changed\n"+
      "    eventIn  SFVec3f set_mintranslation\n"+
      "    eventOut SFVec3f mintranslation_changed\n"+
      "    eventIn  SFVec3f set_maxtranslation\n"+
      "    eventOut SFVec3f maxtranslation_changed\n"+
      "    field    SFFloat fraction 0\n"+
      "    url [\n"+
      "      \"vrmlscript:\n"+
      "        function initialize() {\n"+
      "          max_changed = 1;\n"+
      "        }\n"+
      "        function set_fraction(f) {\n"+
      "          fraction = f;\n"+
      "          translation_changed[0] = f;\n"+
      "        }\n"+
      "        function set_translation(t) {\n"+
      "          fraction_changed = t[0];\n"+
      "          if (fraction_changed < min_changed) {\n"+
      "            min_changed = fraction_changed;\n"+
      "            mintranslation_changed[0] = min_changed;\n"+
      "          }\n"+
      "          if (fraction_changed > max_changed) {\n"+
      "            max_changed = fraction_changed;\n"+
      "            maxtranslation_changed[0] = max_changed;\n"+
      "          }\n"+
      "        }\n"+
      "        function set_mintranslation(t) {\n"+
      "          min_changed = t[0];\n"+
      "          mintranslation_changed[0] = min_changed;\n"+
      "          if (min_changed > max_changed)  {\n"+
      "            max_changed = min_changed;\n"+
      "            maxtranslation_changed[0] = max_changed;\n"+
      "          }\n"+
      "          if (fraction < min_changed) {\n"+
      "            fraction_changed = min_changed;\n"+
      "          }\n"+
      "        }\n"+
      "        function set_maxtranslation(t) {\n"+
      "          max_changed = t[0];\n"+
      "          maxtranslation_changed[0] = max_changed;\n"+
      "          if (max_changed < min_changed)  {\n"+
      "            min_changed = max_changed;\n"+
      "            mintranslation_changed[0] = min_changed;\n"+
      "          }\n"+
      "          if (fraction > max_changed) {\n"+
      "            fraction_changed = max_changed;\n"+
      "          }\n"+
      "        }\n"+
      "      \"  \n"+
      "    ]\n"+
      "  }\n"+
      "ROUTE Clock.time TO Animator.tick\n"+
      "ROUTE Clock.isActive TO Animator.isActive\n"+
      "ROUTE Animator.stopTime TO Clock.stopTime\n"+
      "\n"+
      "ROUTE Animator.startTime TO Clock.startTime\n"+
      "ROUTE Animator.frameCounter TO COUNTERDISPLAY.set_string\n"+
      "ROUTE START.touchTime TO Animator.start\n"+
      "ROUTE LOOP.touchTime TO Animator.loopstart\n"+
      "ROUTE STOP.touchTime TO Animator.stop\n"+
      "ROUTE NEXT.touchTime TO Animator.next\n"+
      "ROUTE PREV.touchTime TO Animator.prev\n"+
      "ROUTE FIRST.touchTime TO Animator.gofirst\n"+
      "ROUTE LAST.touchTime TO Animator.golast\n"+
      "\n"+
      "  # Select Frame and Range\n"+
      "ROUTE Animator.fraction_changed TO Indicator.set_fraction\n"+
      "ROUTE Indicator.translation_changed TO FrameSlider.set_translation\n"+
      "ROUTE Indicator.translation_changed TO FrameSlider.offset\n"+
      "ROUTE FrameSlider.translation_changed TO Indicator.set_translation\n"+
      "ROUTE Indicator.fraction_changed TO Animator.set_fraction\n"+
      "ROUTE MinSlider.translation_changed TO Indicator.set_mintranslation\n"+
      "ROUTE MaxSlider.translation_changed TO Indicator.set_maxtranslation\n"+
      "ROUTE Indicator.mintranslation_changed TO MinSlider.set_translation\n"+
      "ROUTE Indicator.maxtranslation_changed TO MaxSlider.set_translation\n"+
      "ROUTE Indicator.mintranslation_changed TO MinSlider.offset\n"+
      "ROUTE Indicator.maxtranslation_changed TO MaxSlider.offset\n"+
      "ROUTE Indicator.min_changed TO Animator.set_first\n"+
      "ROUTE Indicator.max_changed TO Animator.set_last\n");
  }

}
